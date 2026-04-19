package com.campusshareorder.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId
    private Long id;
    private String operatorType;
    private Long operatorId;
    private String bizType;
    private Long bizId;
    private String action;
    private String detailJson;
    private LocalDateTime createdAt;
}