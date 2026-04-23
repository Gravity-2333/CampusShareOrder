package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MyOrderListItemVO {
    private Long orderId;
    private String orderNo;
    private String productName;
    private String pickupPoint;
    private String status;
    private String myRole;
    private String myJoinStatus;
    private String myPayStatus;
    private String myReceiveStatus;
    private BigDecimal refundAmountTotal;
    private LocalDateTime createdAt;
}