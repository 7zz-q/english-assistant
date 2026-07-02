package com.englishassistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("essays")
public class Essay {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String corrected;
    private Double score;

    // JSON columns — stored as JSON strings, parsed manually in service layer
    private String grammar;
    private String vocabulary;
    private String structure;

    @TableField(insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;
}
