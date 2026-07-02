package com.typingtutor.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that measures service-method execution time.
 *
 * Logs at DEBUG when the method is fast, and at WARN when it exceeds the
 * configurable threshold (default 100 ms). The MDC requestId set by
 * RequestTimingFilter is included so a slow WARN here can be correlated
 * with the enclosing HTTP request's own WARN line.
 *
 * The pointcut covers every public method in every @Service class under
 * com.typingtutor.service. This is intentionally broad — all measurements
 * appear at DEBUG in normal operation, so only genuinely slow calls surface
 * as WARN in production logs without adding noise.
 */
@Aspect
@Component
public class PerformanceLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceLoggingAspect.class);

    @Value("${app.perf.slow-service-threshold-ms:100}")
    private long slowServiceThresholdMs;

    @Around("execution(* com.typingtutor.service.*.*(..))")
    public Object logServiceTiming(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long elapsedMs = System.currentTimeMillis() - start;
            String method = pjp.getSignature().toShortString();
            String rid = MDC.get("requestId");
            String suffix = rid != null ? "  req=" + rid : "";

            if (elapsedMs >= slowServiceThresholdMs) {
                log.warn("[PERF] SERVICE {} took {}ms{}", method, elapsedMs, suffix);
            } else {
                log.debug("[PERF] SERVICE {} took {}ms{}", method, elapsedMs, suffix);
            }
        }
    }
}
