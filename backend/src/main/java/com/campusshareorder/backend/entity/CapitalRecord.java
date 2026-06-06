package com.campusshareorder.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("capital_record")
public class CapitalRecord {
    @TableId
    private Long id;
    private String bizNo;
    private Long userId;
    private Long groupOrderId;
    private Long memberId;
    private String type;
    private BigDecimal amount;
    private String status;
    private String remark;
    private String operatorType;
    private Long operatorId;
    private LocalDateTime createdAt;
}
