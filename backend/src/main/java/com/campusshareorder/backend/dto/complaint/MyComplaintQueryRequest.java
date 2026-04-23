package com.campusshareorder.backend.dto.complaint;

import lombok.Data;

@Data
public class MyComplaintQueryRequest {
    private Integer page = 1;
    private Integer pageSize = 10;
}
