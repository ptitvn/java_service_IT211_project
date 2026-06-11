package com.example.hospitalmanagement.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // Bắt tất cả method trong package service
    @Pointcut("execution(* com.example.hospitalmanagement.service..*(..))")
    public void serviceLayer() {}

    // Bắt tất cả method trong package controller
    @Pointcut("execution(* com.example.hospitalmanagement.controller..*(..))")
    public void controllerLayer() {}

    // Gộp cả hai
    @Pointcut("serviceLayer() || controllerLayer()")
    public void applicationLayer() {}

    @Around("applicationLayer()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.debug("[AOP] --> Entering: {}.{}() with args: {}",
                className, methodName,
                Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();  // Thực thi method thực sự

            long duration = System.currentTimeMillis() - startTime;
            log.info("[PERF] {}.{}() executed in {} ms", className, methodName, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[PERF] {}.{}() failed after {} ms — Error: {}",
                    className, methodName, duration, e.getMessage());
            throw e;
        }
    }

    @AfterReturning(
            pointcut = "serviceLayer()",
            returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.debug("[SUCCESS] {}.{}() completed — result type: {}",
                className, methodName,
                result != null ? result.getClass().getSimpleName() : "void");
    }

    @AfterThrowing(
            pointcut = "serviceLayer()",
            throwing = "exception"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.error("[EXCEPTION] {}.{}() threw {}: {}",
                className, methodName,
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }


    @Before("controllerLayer()")
    public void logBeforeController(JoinPoint joinPoint) {
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("[REQUEST] {}.{}() called", className, methodName);
    }
}
