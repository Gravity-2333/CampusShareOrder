package com.campusshareorder.backend.dto.order;

import lombok.Data;

@Data
public class MyOrderQueryRequest {
    private Integer page = 1;
    private Integer pageSize = 10;
}
