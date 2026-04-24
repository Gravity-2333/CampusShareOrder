package com.campusshareorder.backend.dto.admin;

import lombok.Data;

@Data
public class CapitalRecordQueryRequest {
    private String type;
    private Long userId;
    private Long orderId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
