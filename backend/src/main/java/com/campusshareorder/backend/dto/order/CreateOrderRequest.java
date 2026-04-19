package com.campusshareorder.backend.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    private String productName;
    private String productDesc;
    private Integer totalMemberCount;
    private BigDecimal estimatedTotalAmount;
    private String pickupPoint;
    private String deadlineAt;
}