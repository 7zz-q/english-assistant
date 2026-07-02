package com.englishassistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("daily_stats")
public class DailyStat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    @TableField("`date`")
    private LocalDate date;
    private Integer wordsLearned;
    private Integer wordsReviewed;
    private Integer essaysWritten;
    private Integer minutesSpent;
}
