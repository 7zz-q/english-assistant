package com.englishassistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Type-safe configuration for JWT.
 * Replaces @Value("${jwt.secret}") and @Value("${jwt.expiration}").
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /** JWT signing secret (HMAC-SHA) */
    private String secret = "english-assistant-jwt-secret-2024";
    /** Token expiration in milliseconds (default 7 days) */
    private long expiration = 604800000;
}
