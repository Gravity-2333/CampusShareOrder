package com.campusshareorder.backend.dto.order;

import lombok.Data;

@Data
public class OrderQueryRequest {
    private String keyword;
    private String status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
