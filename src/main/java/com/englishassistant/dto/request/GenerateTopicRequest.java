package com.englishassistant.dto.request;

import lombok.Data;

@Data
public class GenerateTopicRequest {
    private String level = "cet6";  // cet4, cet6, 考研
}
