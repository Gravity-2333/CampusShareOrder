package com.campusshareorder.backend.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOperationLogVO {
    private String action;
    private String operatorName;
    private String targetNo;
    private LocalDateTime createdAt;
}
