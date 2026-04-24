package com.campusshareorder.backend.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOperationLogVO {
    private String action;
    private String operatorName;
    private String operatorType;
    private String bizType;
    private Long bizId;
    private String targetNo;
    private String detail;
    private LocalDateTime createdAt;
}
