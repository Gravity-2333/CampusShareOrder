package com.campusshareorder.backend.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCapitalRecordVO {
    private String bizNo;
    private Long orderId;
    private String orderNo;
    private String type;
    private BigDecimal amount;
    private String status;
    private String remark;
    private String operatorName;
    private String receiverName;
    private LocalDateTime createdAt;
}
