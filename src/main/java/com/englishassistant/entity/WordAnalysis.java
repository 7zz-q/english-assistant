package com.englishassistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("word_analysis")
public class WordAnalysis {
    @TableId(type = IdType.INPUT)
    private String word;
    private String data;
    private LocalDateTime createdAt;
}
