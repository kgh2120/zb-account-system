package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.repository.AccountRepository;
import com.example.account.type.ErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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


}