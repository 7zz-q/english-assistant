package com.englishassistant.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ReviewRequest {
    @Min(0) @Max(4)
    private int quality;  // 0=forgot 1=vague 2=remembered 3=familiar 4=mastered
}
