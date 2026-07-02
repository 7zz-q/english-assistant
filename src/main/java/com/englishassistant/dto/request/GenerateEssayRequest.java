package com.englishassistant.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateEssayRequest {
    @NotBlank(message = "题目不能为空")
    private String topic;
    private String level = "cet6";
}
