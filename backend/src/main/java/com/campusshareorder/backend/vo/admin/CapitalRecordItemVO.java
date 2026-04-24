package com.campusshareorder.backend.vo.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CapitalRecordItemVO {
    private Long id;
    private String bizNo;
    private Long userId;
    private Long groupOrderId;
    private Long memberId;
    private String type;
    private BigDecimal amount;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private String userNickname;
}
