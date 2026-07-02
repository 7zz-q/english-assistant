package com.englishassistant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TranslateRequest {
    @NotBlank(message = "文本不能为空")
    @Size(min = 2, message = "文本至少2个字符")
    private String text;
    private String direction = "auto";  // auto, en2zh, zh2en
}
