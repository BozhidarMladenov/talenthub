package com.softuni.statssvc.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    @Pointcut("execution(public * com.softuni.statssvc.service.*.*(..))")
    public void serviceLayerMethods() {
    }

    @Around("serviceLayerMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > 500) {
                log.warn("[AOP] {}.{}() completed in {}ms (slow operation)", className, methodName, elapsed);
            } else {
                log.debug("[AOP] {}.{}() completed in {}ms", className, methodName, elapsed);
            }
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[AOP] {}.{}() threw {} after {}ms: {}",
                    className, methodName, ex.getClass().getSimpleName(), elapsed, ex.getMessage());
            throw ex;
        }
    }
}
