package com.englishassistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    @TableField(insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;
}
