package com.campusshareorder.backend.dto.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UploadReceiptRequest {
    private String imageUrl;
    private BigDecimal actualTotalAmount;
    private LocalDateTime expectedDeliveryStartAt;
    private LocalDateTime expectedDeliveryEndAt;
}