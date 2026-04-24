package com.campusshareorder.backend.dto.admin;

import lombok.Data;

@Data
public class AdminOrderQueryRequest {
    private String status;
    private String orderNo;
    private Integer page = 1;
    private Integer pageSize = 10;
}
