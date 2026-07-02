package com.englishassistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_papers")
public class ExamPaper {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("`year`")
    private Integer year;
    @TableField("`month`")
    private Integer month;
    private String title;
    @TableField("`type`")
    private String type;
    private String sections;  // JSON string

    @TableField(insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;
}
