package com.englishassistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("words")
public class Word {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String word;
    private String phonetic;
    private String meaning;
    private String example;
    @TableField("`level`")
    private String level;
    @TableField("`status`")
    private Integer status;        // 0=new 1=learning 2=mastered
    private Integer reviewCount;
    private LocalDate nextReview;
    private Double easeFactor;     // default 2.5
    @TableField("`interval`")
    private Integer interval;      // days

    @TableField(insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;
}
