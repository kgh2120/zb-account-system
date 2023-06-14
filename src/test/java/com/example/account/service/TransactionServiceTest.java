package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    public static final long AMOUNT = 100L;
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;


    @Test
    void useBalanceSuccess() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountUser(user)
                .accountNumber("10004")
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account)
                );
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .transactionType(TransactionType.USE)
                        .transactionResultType(TransactionResultType.S)
                        .amount(100L)
                        .balanceSnapshot(900L)
                        .account(account).build());
        //when
        TransactionDto transactionDto = transactionService.useBalance(1L, "10004", 100L);

        //then
        assertThat(transactionDto.getTransactionResultType()).isEqualTo(TransactionResultType.S);
        assertThat(transactionDto.getTransactionType()).isEqualTo(TransactionType.USE);
        assertThat(transactionDto.getAccountNumber()).isEqualTo("10004");
        assertThat(transactionDto.getAmount()).isEqualTo(AMOUNT);
        assertThat(transactionDto.getBalanceSnapshot()).isEqualTo(900L);

    }

    @DisplayName("잔액 사용 - 실패 [유저가 없는 케이스]")
    @Test
    void useBalance_Fail_UserNotFound() throws Exception {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> transactionService.useBalance(1L, "10000000", 1000L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @DisplayName("잔액 사용 - 실패 [계좌 번호에 맞는 계좌가 없는 케이스]")
    @Test
    void useBalance_Fail_AccountNotFound() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty()
                );
        //when
        //then
        assertThatThrownBy(() -> transactionService.useBalance(1L, "1000000000", 1000L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.ACCOUNT_NOT_FOUND.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @DisplayName("잔액 사용 - 실패 [유저가 계좌의 주인이 아닌 경우]")
    @Test
    void unRegisterAccount_Fail_AccountOwnerUnMatch() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(2L)
                .name("Kim")
                .build();

        AccountUser anotherUser = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .id(1L)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(0L)
                        .accountUser(anotherUser)
                        .accountNumber("10004")
                        .build())
                );
        //when
        //then
        assertThatThrownBy(() -> transactionService.useBalance(1L, "1000000000", 1000L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.ACCOUNT_OWNER_UN_MATCH.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_OWNER_UN_MATCH);
    }

    @DisplayName("잔액 사용 - 실패 [계좌가 이미 해지된 경우]")
    @Test
    void unRegisterAccount_Fail_AlreadyUnRegistered() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .id(1L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(0L)
                        .accountUser(user)
                        .accountNumber("10004")
                        .build())
                );
        //when
        //then
        assertThatThrownBy(() -> transactionService.useBalance(1L, "1000000000", 1000L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
    }

    @DisplayName("잔액 사용 - 실패 [사용 금액이 잔액보다 큰 경우]")
    @Test
    void useBalanceFail_AMOUNT_EXCEED_BALANCE() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountUser(user)
                .accountNumber("10004")
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account)
                );
        //when
        assertThatThrownBy(() -> transactionService.useBalance(1L, "10004", 10000L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.AMOUNT_EXCEED_BALANCE.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AMOUNT_EXCEED_BALANCE);

    }

    @DisplayName("실패 트랜잭션 저장 - 성공")
    @Test
    void saveFailedTransactionSuccess() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();
        Account account = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("100000000")
                .accountUser(user)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account)
                );

        //when    //then
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        transactionService.saveFailedUseTransaction("100000000", AMOUNT);

        verify(transactionRepository, times(1))
                .save(captor.capture());

        Transaction capture = captor.getValue();
        assertThat(capture.getTransactionResultType()).isEqualTo(TransactionResultType.F);
        assertThat(capture.getAmount()).isEqualTo(AMOUNT);
        assertThat(capture.getTransactionType()).isEqualTo(TransactionType.USE);
        assertThat(capture.getBalanceSnapshot()).isEqualTo(10000L);


    }

    @DisplayName("실패 트랜잭션 저장 - 실패 [계좌가 없음]")
    @Test
    void saveFailedTransactionFail_ACCOUNT_NOT_FOUND() throws Exception {
        //given
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty()
                );

        assertThatThrownBy(() -> transactionService.saveFailedUseTransaction("100000000", AMOUNT))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.ACCOUNT_NOT_FOUND.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);



    }

    @Test
    void cancelBalanceSuccess() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountUser(user)
                .accountNumber("10004")
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.CANCEL)
                .transactionId("transactionId")
                .transactionResultType(TransactionResultType.S)
                .amount(100L)
                .balanceSnapshot(900L)
                .transactedAt(LocalDateTime.now())
                .account(account).build();
        given(transactionRepository.save(any()))
                .willReturn(tx);

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(tx));

        //when
        TransactionDto transactionDto = transactionService.cancelBalance("transactionId", "10004", 100L);

        //then
        assertThat(transactionDto.getTransactionResultType()).isEqualTo(TransactionResultType.S);
        assertThat(transactionDto.getTransactionType()).isEqualTo(TransactionType.CANCEL);
        assertThat(transactionDto.getAccountNumber()).isEqualTo("10004");
        assertThat(transactionDto.getAmount()).isEqualTo(AMOUNT);
        assertThat(transactionDto.getBalanceSnapshot()).isEqualTo(900L);

    }

    @DisplayName("잔액 취소 실패 - 거래ID에 맞는 거래가 존재하지 않음")
    @Test
    void cancelBalanceFail_TransactionNotFound () throws Exception{
        //given
        given(transactionRepository.findByTransactionId(any()))
                .willReturn(Optional.empty());
        //when       //then
        assertThatThrownBy(()-> transactionService.cancelBalance("transactionId",
                "1000000000",1000L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.TRANSACTION_NOT_FOUND.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRANSACTION_NOT_FOUND);
    }


    @Test
    @DisplayName("잔액 취소 실패 - 계좌 번호에 해당하는 계좌가 존재하지 않음.")
    void cancelBalanceFail_AccountNotFound() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountUser(user)
                .accountNumber("10004")
                .build();



        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.CANCEL)
                .transactionId("transactionId")
                .transactionResultType(TransactionResultType.S)
                .amount(100L)
                .balanceSnapshot(900L)
                .transactedAt(LocalDateTime.now())
                .account(account).build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(tx));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        assertThatThrownBy(()-> transactionService.cancelBalance("transactionId",
                "1000000000",1000L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.ACCOUNT_NOT_FOUND.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);

    }

    @Test
    @DisplayName("잔액 취소 실패 - 계좌 번호가 서로 맞지 않음.")
    void cancelBalanceFail_TRANSACTION_ACCOUNT_UN_MATCH() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountUser(user)
                .accountNumber("10004")
                .build();

        Account txAccount = Account.builder()
                .id(2L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountUser(user)
                .accountNumber("10004123123")
                .build();



        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.CANCEL)
                .transactionId("transactionId")
                .transactionResultType(TransactionResultType.S)
                .amount(100L)
                .balanceSnapshot(900L)
                .transactedAt(LocalDateTime.now())
                .account(txAccount).build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(tx));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        assertThatThrownBy(()-> transactionService.cancelBalance("transactionId",
                "1000000000",1000L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH);

    }

    @Test
    @DisplayName("잔액 취소 실패 - 거래 금액이 맞지 않음")
    void cancelBalanceFail_TRANSACTION_AMOUNT_UN_MATCH() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountUser(user)
                .accountNumber("10004")
                .build();




        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.CANCEL)
                .transactionId("transactionId")
                .transactionResultType(TransactionResultType.S)
                .amount(100L)
                .balanceSnapshot(900L)
                .transactedAt(LocalDateTime.now())
                .account(account).build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(tx));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        assertThatThrownBy(()-> transactionService.cancelBalance("transactionId",
                "1000000000",1000L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.TRANSACTION_AMOUNT_UN_MATCH.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRANSACTION_AMOUNT_UN_MATCH);

    }
    @Test
    @DisplayName("잔액 취소 실패 - 너무 과거의 거래임")
    void cancelBalanceFail_TOO_OLD_TRANSACTION_TO_CANCEL() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountUser(user)
                .accountNumber("10004")
                .build();




        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.CANCEL)
                .transactionId("transactionId")
                .transactionResultType(TransactionResultType.S)
                .amount(100L)
                .balanceSnapshot(900L)
                .transactedAt(LocalDateTime.of(1980,2,25,1,12))
                .account(account).build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(tx));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        assertThatThrownBy(()-> transactionService.cancelBalance("transactionId",
                "1000000000",100L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.TOO_OLD_TRANSACTION_TO_CANCEL.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_OLD_TRANSACTION_TO_CANCEL);

    }
    @Test
    @DisplayName("잔액 취소 실패 - 거래 금액이 0 미만의 금액이다")
    void cancelBalanceFail_INVALID_REQUEST() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountUser(user)
                .accountNumber("10004")
                .build();




        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.CANCEL)
                .transactionId("transactionId")
                .transactionResultType(TransactionResultType.S)
                .amount(-100L)
                .balanceSnapshot(900L)
                .transactedAt(LocalDateTime.now())
                .account(account).build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(tx));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        assertThatThrownBy(()-> transactionService.cancelBalance("transactionId",
                "1000000000",-100L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.INVALID_REQUEST.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

    }

    @Test
    @DisplayName("거래 조회 - 성공")
    void queryTransactionSuccess() throws Exception {
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountUser(user)
                .accountNumber("10004")
                .build();


        String transactionId = "transactionId";
        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.CANCEL)
                .transactionId(transactionId)
                .transactionResultType(TransactionResultType.S)
                .amount(100L)
                .balanceSnapshot(900L)
                .transactedAt(LocalDateTime.now())
                .account(account).build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(tx));

        TransactionDto dto = transactionService.queryTransaction(transactionId);

        assertThat(dto.getTransactionId()).isEqualTo(transactionId);
        assertThat(dto.getAmount()).isEqualTo(100L);
        assertThat(dto.getTransactionType()).isEqualTo(TransactionType.CANCEL);
        assertThat(dto.getTransactionResultType()).isEqualTo(TransactionResultType.S);
        assertThat(dto.getAccountNumber()).isEqualTo("10004");
        assertThat(dto.getBalanceSnapshot()).isEqualTo(900L);
    }
    @Test
    @DisplayName("거래 조회 - 실패 거래를 찾을 수 없습니다.")
    void queryTransactionFail_TransactionNotFound() throws Exception {
        //given
        String transactionId = "transactionId";

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        assertThatThrownBy(()-> transactionService.queryTransaction(transactionId))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.TRANSACTION_NOT_FOUND.getDescription())
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRANSACTION_NOT_FOUND);
    }

}