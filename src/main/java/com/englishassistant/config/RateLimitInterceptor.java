package com.englishassistant.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;

/**
 * Simple Redis-based rate limiter for AI endpoints.
 * Demonstrates Redis usage + interceptor pattern for Java internship interviews.
 */
@Component
@Profile("!test")
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimitInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                            Object handler) throws Exception {
        String path = request.getRequestURI();

        // Only limit AI-heavy endpoints
        if (!path.startsWith("/api/writing/") && !path.startsWith("/api/translate")) {
            return true;
        }

        // Use client IP as key (for non-logged-in requests) or request path
        String clientIp = request.getRemoteAddr();
        String key = "ratelimit:" + path + ":" + clientIp;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        // Max 30 requests per minute per client per endpoint
        if (count != null && count > 30) {
            log.warn("Rate limit exceeded for {} from {}", path, clientIp);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"请求过于频繁，请稍后再试\"}");
            return false;
        }

        return true;
    }
}
