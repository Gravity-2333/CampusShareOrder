package com.campusshareorder.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("admin_log")
public class AdminLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long adminId;

    private String action;

    private String targetType;

    private Long targetId;

    private String description;

    private String ipAddress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
