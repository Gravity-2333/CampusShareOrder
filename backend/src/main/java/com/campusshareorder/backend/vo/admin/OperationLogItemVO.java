package com.campusshareorder.backend.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogItemVO {
    private Long id;
    private String operatorType;
    private Long operatorId;
    private String bizType;
    private Long bizId;
    private String action;
    private String detailJson;
    private LocalDateTime createdAt;
    private String operatorName;
}
