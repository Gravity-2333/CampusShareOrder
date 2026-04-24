package com.campusshareorder.backend.vo.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminCapitalRecordVO {
    private String bizNo;
    private Long orderId;
    private String orderNo;
    private String type;
    private BigDecimal amount;
    private String status;
    private String remark;
    private String userNickname;
    private LocalDateTime createdAt;
}
