package com.englishassistant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CorrectEssayRequest {
    private String title;

    @NotBlank(message = "作文内容不能为空")
    @Size(min = 20, message = "作文至少20个字符")
    private String content;
}
