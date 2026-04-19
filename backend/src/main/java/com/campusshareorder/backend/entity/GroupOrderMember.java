package com.campusshareorder.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("group_order_member")
public class GroupOrderMember {
    @TableId
    private Long id;
    private Long groupOrderId;
    private Long userId;
    private Boolean isCreator;
    private String remark;
    private String joinStatus;
    private String payStatus;
    private BigDecimal payAmount;
    private LocalDateTime paidAt;
    private BigDecimal refundAmountTotal;
    private String receiveStatus;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}