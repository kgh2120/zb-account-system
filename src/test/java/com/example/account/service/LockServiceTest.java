package com.example.account.service;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {

    @Mock
    RedissonClient redissonClient;

    @Mock
    RLock rLock;

    @InjectMocks
    LockService lockService;


    @Test
    void successGetLock () throws Exception{
        //given
        given(redissonClient.getLock(anyString()))
                .willReturn(rLock);

        given(rLock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(true);
        //when
        //then
        Assertions.assertThatNoException()
                .isThrownBy(() -> lockService.lock("123"));

    }
    @Test
    void failGetLock () throws Exception{
        //given
        given(redissonClient.getLock(anyString()))
                .willReturn(rLock);

        given(rLock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(false);
        //when
        //then
        Assertions.assertThatThrownBy(() -> lockService.lock("123"))
                .isInstanceOf(AccountException.class)
                .hasMessage(ErrorCode.ACCOUNT_TRANSACTION_LOCK.getDescription());

    }

}