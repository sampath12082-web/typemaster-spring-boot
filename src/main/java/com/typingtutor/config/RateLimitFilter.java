package com.typingtutor.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet filter that applies per-IP rate limiting to sensitive auth endpoints
 * using Bucket4j token-bucket algorithm.
 *
 * <p>Rate limits:
 * <ul>
 *   <li>{@code /api/auth/login} — 10 requests per minute</li>
 *   <li>{@code /api/auth/forgot-password} — 5 requests per 10 minutes</li>
 *   <li>{@code /api/auth/resend-otp} — 5 requests per 10 minutes</li>
 *   <li>{@code /api/auth/verify-otp} — 10 requests per minute</li>
 * </ul>
 *
 * <p>All other endpoints pass through without rate limiting.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final String RATE_LIMIT_ERROR_JSON =
            "{\"error\":\"Too many requests. Please try again later.\"}";

    /**
     * Each entry maps a URI path to its rate-limit configuration.
     */
    private record RateLimitRule(long tokens, Duration period) {}

    private static final Map<String, RateLimitRule> RULES = Map.of(
            "/api/auth/login",           new RateLimitRule(10, Duration.ofMinutes(1)),
            "/api/auth/forgot-password", new RateLimitRule(5,  Duration.ofMinutes(10)),
            "/api/auth/resend-otp",      new RateLimitRule(5,  Duration.ofMinutes(10)),
            "/api/auth/verify-otp",      new RateLimitRule(10, Duration.ofMinutes(1))
    );

    /**
     * One bucket map per rate-limited path. Keyed by client IP.
     */
    private final Map<String, ConcurrentHashMap<String, Bucket>> bucketMaps = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        for (String path : RULES.keySet()) {
            bucketMaps.put(path, new ConcurrentHashMap<>());
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        String path = httpRequest.getRequestURI();
        RateLimitRule rule = RULES.get(path);

        if (rule == null) {
            // No rate limit for this endpoint
            chain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(httpRequest);
        Bucket bucket = resolveBucket(path, clientIp, rule);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            logger.warn("Rate limit exceeded for IP {} on endpoint {}", clientIp, path);
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.getWriter().write(RATE_LIMIT_ERROR_JSON);
        }
    }

    private Bucket resolveBucket(String path, String clientIp, RateLimitRule rule) {
        return bucketMaps.get(path).computeIfAbsent(clientIp, ip -> createBucket(rule));
    }

    private Bucket createBucket(RateLimitRule rule) {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(rule.tokens(), rule.period()))
                .build();
    }

    /**
     * Resolves the client IP address, respecting the {@code X-Forwarded-For} header
     * set by reverse proxies (e.g., Render, Nginx).
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For may contain multiple IPs: "client, proxy1, proxy2"
            // The first one is the original client IP.
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
