package com.englishassistant.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP logging aspect — records method execution time and request info for all controllers.
 * Demonstrates AOP usage for Java internship interviews.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.englishassistant.controller..*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String method = joinPoint.getSignature().toShortString();

        Object result = joinPoint.proceed();

        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > 500) {
            log.warn("[SLOW] {} took {}ms", method, elapsed);
        }
        return result;
    }

    @Around("execution(* com.englishassistant.service.impl.AiServiceImpl.*(..))")
    public Object logAiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String method = joinPoint.getSignature().getName();

        Object result = joinPoint.proceed();

        long elapsed = System.currentTimeMillis() - start;
        log.info("[AI] {}(…) completed in {}ms", method, elapsed);
        return result;
    }
}
