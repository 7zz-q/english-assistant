package com.englishassistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("seed_words")
public class SeedWord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private String meaning;
    @TableField("`level`")
    private String level;
}
