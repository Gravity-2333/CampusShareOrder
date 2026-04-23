package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderBasicInfoVO {
    private Long orderId;
    private String orderNo;
    private String productName;
    private String productDesc;
    private Integer totalMemberCount;
    private Integer currentMemberCount;
    private BigDecimal estimatedTotalAmount;
    private BigDecimal estimatedPerAmount;
    private BigDecimal actualTotalAmount;
    private BigDecimal actualPerAmount;
    private String pickupPoint;
    private String status;
    private LocalDateTime deadlineAt;
    private LocalDateTime receiptUploadDeadlineAt;
    private LocalDateTime expectedDeliveryStartAt;
    private LocalDateTime expectedDeliveryEndAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
}