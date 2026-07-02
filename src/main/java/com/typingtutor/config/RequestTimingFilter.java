package com.typingtutor.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.UUID;

/**
 * Wraps every HTTP request to record wall-clock duration.
 *
 * Logs at DEBUG for fast requests and WARN for requests that exceed the
 * configurable threshold (default 500 ms). Each request also gets a short
 * random ID that propagates via MDC so filter, AOP, and Hibernate slow-query
 * log lines for the same request are all correlated.
 *
 * Order Ordered.HIGHEST_PRECEDENCE guarantees this filter runs before
 * RateLimitFilter (HIGHEST_PRECEDENCE+1) and JwtAuthFilter, so the
 * measured time includes rate-limit checks and JWT validation.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTimingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTimingFilter.class);

    // Request attribute written by JwtAuthFilter once the token is validated.
    public static final String AUTH_USER_ATTR = "perf.authenticated-user";

    @Value("${app.perf.slow-request-threshold-ms:500}")
    private long slowRequestThresholdMs;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        long start = System.currentTimeMillis();

        MDC.put("requestId", requestId);
        // Expose requestId to client so it can be correlated with support reports
        response.setHeader("X-Request-Id", requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            long elapsedMs = System.currentTimeMillis() - start;
            String user = (String) request.getAttribute(AUTH_USER_ATTR);
            if (user == null) user = "anonymous";

            if (elapsedMs >= slowRequestThresholdMs) {
                log.warn("[PERF] {} {} -> {} in {}ms  user={}  req={}",
                        request.getMethod(), request.getRequestURI(),
                        response.getStatus(), elapsedMs, user, requestId);
            } else {
                log.debug("[PERF] {} {} -> {} in {}ms  user={}  req={}",
                        request.getMethod(), request.getRequestURI(),
                        response.getStatus(), elapsedMs, user, requestId);
            }

            MDC.clear();
        }
    }
}
