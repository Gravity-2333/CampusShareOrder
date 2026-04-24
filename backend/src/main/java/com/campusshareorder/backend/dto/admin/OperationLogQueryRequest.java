package com.campusshareorder.backend.dto.admin;

import lombok.Data;

@Data
public class OperationLogQueryRequest {
    private String bizType;
    private Long bizId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
