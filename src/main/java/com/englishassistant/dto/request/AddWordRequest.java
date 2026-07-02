package com.englishassistant.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddWordRequest {
    @NotBlank(message = "单词不能为空")
    private String word;
    private String meaning;
    private String level = "cet6";
}
