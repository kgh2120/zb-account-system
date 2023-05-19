package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.repository.AccountRepository;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountSuccess () throws Exception{
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("10002").build()));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .id(1L)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(10000L)
                        .accountUser(user)
                        .accountNumber("10004")
                        .build()
                );

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountDto accountDto = accountService.createAccount(1L, 10000L);

        //then
        verify(accountRepository,times(1))
                .save(captor.capture());
        assertThat(captor.getValue().getAccountNumber()).isEqualTo("10003");
        assertThat(accountDto.getBalance()).isEqualTo(10000L);
    }

    @Test
    void createFirstAccountSuccess () throws Exception{
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .id(1L)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(10000L)
                        .accountUser(user)
                        .accountNumber("1000000000")
                        .build()
                );

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountDto accountDto = accountService.createAccount(1L, 10000L);

        //then
        verify(accountRepository,times(1))
                .save(captor.capture());
        assertThat(captor.getValue().getAccountNumber()).isEqualTo("1000000000");
        assertThat(accountDto.getBalance()).isEqualTo(10000L);
    }

    @DisplayName("계좌 생성 - 실패 [유저가 없는 케이스]")
    @Test
    void createAccount_Fail_UserNotFound () throws Exception{
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> accountService.createAccount(1L, 1000L))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getDescription())
                .hasFieldOrPropertyWithValue("errorCode",ErrorCode.USER_NOT_FOUND);
    }

    @DisplayName("계좌 생성 - 실패 [계좌 보유량 한도 초과 케이스]")
    @Test
    void createAccount_Fail_OverMaxAccountSize () throws Exception{
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);
        //when
        assertThatThrownBy(() -> accountService.createAccount(1L, 1000L))
                .isInstanceOf(AccountException.class)
                .hasFieldOrPropertyWithValue("errorCode",ErrorCode.EXCEED_MAX_ACCOUNT_SIZE);

        //then


    }


    @Test
    void unRegisterAccountSuccess () throws Exception{
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
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(0L)
                        .accountUser(user)
                        .accountNumber("10004")
                        .build())
                );


        //when
        AccountDto accountDto = accountService.unRegisterAccount(1L, "100000000");

        //then
        assertThat(accountDto.getUnRegisteredAt()).isNotNull();
        assertThat(accountDto.getBalance()).isZero();
    }

    @DisplayName("계좌 해지 - 실패 [유저가 없는 케이스]")
    @Test
    void unRegisterAccount_Fail_UserNotFound() throws Exception{
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> accountService.unRegisterAccount(1L, "1000000000"))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getDescription())
                .hasFieldOrPropertyWithValue("errorCode",ErrorCode.USER_NOT_FOUND);
    }

    @DisplayName("계좌 해지 - 실패 [계좌 번호에 맞는 계좌가 없는 케이스]")
    @Test
    void unRegisterAccount_Fail_AccountNotFound() throws Exception{
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
        assertThatThrownBy(() -> accountService.unRegisterAccount(1L, "1000000000"))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.ACCOUNT_NOT_FOUND.getDescription())
                .hasFieldOrPropertyWithValue("errorCode",ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @DisplayName("계좌 해지 - 실패 [유저가 계좌의 주인이 아닌 경우]")
    @Test
    void unRegisterAccount_Fail_AccountOwnerUnMatch() throws Exception{
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
        assertThatThrownBy(() -> accountService.unRegisterAccount(2L, "1000000000"))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.ACCOUNT_OWNER_UN_MATCH.getDescription())
                .hasFieldOrPropertyWithValue("errorCode",ErrorCode.ACCOUNT_OWNER_UN_MATCH);
    }

    @DisplayName("계좌 해지 - 실패 [계좌에 잔액이 남은 경우]")
    @Test
    void unRegisterAccount_Fail_BalanceRemain() throws Exception{
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
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(1000L)
                        .accountUser(user)
                        .accountNumber("10004")
                        .build())
                );
        //when
        //then
        assertThatThrownBy(() -> accountService.unRegisterAccount(1L, "1000000000"))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.BALANCE_REMAIN.getDescription())
                .hasFieldOrPropertyWithValue("errorCode",ErrorCode.BALANCE_REMAIN);
    }

    @DisplayName("계좌 해지 - 실패 [계좌가 이미 해지된 경우]")
    @Test
    void unRegisterAccount_Fail_AlreadyUnRegistered() throws Exception{
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
        assertThatThrownBy(() -> accountService.unRegisterAccount(1L, "1000000000"))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED.getDescription())
                .hasFieldOrPropertyWithValue("errorCode",ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
    }


    @DisplayName("계좌 조회 - 성공")
    @Test
    void getAllAccountSuccess() throws Exception{
        //given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("Kim")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        Account account1 = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountUser(user)
                .accountNumber("1111111111")
                .build();
        Account account2 = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(2000L)
                .accountUser(user)
                .accountNumber("2222222222")
                .build();
        Account account3 = Account.builder()
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(3000L)
                .accountUser(user)
                .accountNumber("3333333333")
                .build();
        given(accountRepository.findByAccountUser(any()))
                .willReturn(List.of(
                        account1,
                        account2,
                        account3
                        )
                );

        //when
        List<AccountDto> accountInfo = accountService.getAllAccountInfo(1L);
        //then
        assertThat(accountInfo)
                .hasSize(3).
                contains(AccountDto.fromEntity(account1),
                        AccountDto.fromEntity(account2),
                        AccountDto.fromEntity(account3));
    }

    @DisplayName("계좌 조회 - 실패 [유저가 없는 케이스]")
    @Test
    void getAllAccount_fail_UserNotFound () throws Exception{
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        assertThatThrownBy(() -> accountService.getAllAccountInfo(1L))
                .isInstanceOf(AccountException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        //then


    }

}