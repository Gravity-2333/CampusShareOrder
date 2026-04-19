package com.campusshareorder.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_receipt")
public class OrderReceipt {
    @TableId
    private Long id;
    private Long groupOrderId;
    private Long uploaderUserId;
    private String imageUrl;
    private BigDecimal actualTotalAmount;
    private LocalDateTime expectedDeliveryStartAt;
    private LocalDateTime expectedDeliveryEndAt;
    private LocalDateTime uploadedAt;
}