package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReceiptInfoVO {
    private Long receiptId;
    private String imageUrl;
    private BigDecimal actualTotalAmount;
    private LocalDateTime expectedDeliveryStartAt;
    private LocalDateTime expectedDeliveryEndAt;
    private LocalDateTime uploadedAt;
    private String uploaderNickname;
}