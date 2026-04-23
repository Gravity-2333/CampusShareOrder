package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentSummaryVO {
    private BigDecimal estimatedTotalAmount;
    private BigDecimal actualTotalAmount;
    private Integer totalMembers;
    private Integer paidMembers;
    private BigDecimal totalPaidAmount;
    private BigDecimal refundTotalAmount;
}
