package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateOrderVO {
    private Long orderId;
    private String orderNo;
    private String status;
    private LocalDateTime createdAt;
}