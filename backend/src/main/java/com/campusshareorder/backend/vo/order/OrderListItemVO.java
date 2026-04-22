package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderListItemVO {
    private Long orderId;
    private String orderNo;
    private String productName;
    private String productDesc;
    private Integer totalMemberCount;
    private Integer currentMemberCount;
    private Integer remainingCount;
    private BigDecimal estimatedPerAmount;
    private String pickupPoint;
    private String status;
    private LocalDateTime deadlineAt;
    private LocalDateTime createdAt;
    private String creatorNickname;
}