package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderMemberVO {
    private Long userId;
    private String nickname;
    private Boolean isCreator;
    private String remark;
    private String joinStatus;
    private String payStatus;
    private BigDecimal payAmount;
    private LocalDateTime paidAt;
    private BigDecimal refundAmountTotal;
    private String receiveStatus;
    private LocalDateTime receivedAt;
}