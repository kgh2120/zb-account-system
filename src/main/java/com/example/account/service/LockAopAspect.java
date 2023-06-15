package com.example.account.service;

import com.example.account.aop.AccountLockId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {

    private final LockService lockService;

    @Around("@annotation(com.example.account.aop.AccountLock) && args(request)")
    public Object aroundMethod(ProceedingJoinPoint pjp,
                               AccountLockId request)
            throws Throwable {
        // lock 획득 시도
        try{
            // before
            lockService.lock(request.getAccountNumber());
            return pjp.proceed();
        }finally {
            // lock 해제 after
            lockService.unlock(request.getAccountNumber());
        }

    }


}
