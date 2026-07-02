package com.englishassistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reading_logs")
public class ReadingLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String wordsLookedUp;  // JSON string
    private LocalDateTime createdAt;
}
