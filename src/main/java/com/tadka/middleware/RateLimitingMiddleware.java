package com.tadka.middleware;

import com.tadka.infrastructure.ratelimiting.IRateLimiter;
import com.tadka.infrastructure.ratelimiting.RateLimitResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Day 6, Beat (ADR-049): per-IP, Redis-backed (shared across every replica).
 * No JWT exists yet (Day 10 adds auth) so IP is the only identity available.
 * A 429 always carries Retry-After so a well-behaved client backs off.
 * Redis down: fail OPEN (allow the request).
 */
@Component
public class RateLimitingMiddleware implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingMiddleware.class);

    private final IRateLimiter limiter;

    public RateLimitingMiddleware(IRateLimiter limiter) {
        this.limiter = limiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
        RateLimitResult result;
        try {
            result = limiter.checkRateLimit(ip);
        } catch (DataAccessException ex) {
            log.warn("Rate limiter Redis unavailable; failing open.");
            return true;
        }

        if (!result.allowed()) {
            int retryAfterSeconds = Math.max(1, (int) Math.ceil(result.retryAfterSeconds()));
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"Too Many Requests\",\"detail\":\"Rate limit exceeded. Retry after %ds.\",\"status\":429}",
                retryAfterSeconds));
            return false;
        }

        return true;
    }
}
