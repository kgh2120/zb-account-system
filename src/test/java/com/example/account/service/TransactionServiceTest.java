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

}