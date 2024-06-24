package com.example.Account2.service;

import com.example.Account2.aop.AccountLockIdInterface;
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
    @Around("@annotation(com.example.Account2.aop.AccountLock) && args(request)")
    public Object aroundMethod(
            ProceedingJoinPoint pjp,
            AccountLockIdInterface request
    ) throws  Throwable{
        lockService.lock(request.getAccountNumber());
        try {
            //before
            return pjp.proceed();
        } finally {
            lockService.unlock(request.getAccountNumber());

        }
    }
}
