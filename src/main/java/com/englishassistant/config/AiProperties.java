package com.englishassistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Type-safe configuration for DashScope AI API.
 * Replaces scattered @Value annotations in AiServiceImpl.
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.dashscope")
public class AiProperties {
    /** DashScope API key (通义千问) */
    private String apiKey = "sk-placeholder";
    /** DashScope compatible API base URL */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    /** Primary model for complex tasks (essay correction, essay generation) */
    private String model = "qwen-plus";
    /** Fast model for simple tasks (word analysis, translation) */
    private String fastModel = "qwen-turbo";
}
