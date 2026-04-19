package com.campusshareorder.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("group_order")
public class GroupOrder {
    @TableId
    private Long id;
    private String orderNo;
    private Long creatorUserId;
    private String productName;
    private String productDesc;
    private Integer totalMemberCount;
    private Integer currentMemberCount;
    private BigDecimal estimatedTotalAmount;
    private BigDecimal estimatedPerAmount;
    private BigDecimal actualTotalAmount;
    private BigDecimal actualPerAmount;
    private String pickupPoint;
    private LocalDateTime deadlineAt;
    private LocalDateTime receiptUploadDeadlineAt;
    private LocalDateTime expectedDeliveryStartAt;
    private LocalDateTime expectedDeliveryEndAt;
    private LocalDateTime deliveredAt;
    private String status;
    private String cancelReason;
    private Boolean complaintOpened;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}