package com.campusshareorder.backend.dto.admin;

import lombok.Data;

@Data
public class AdminUserQueryRequest {
    private String keyword;
    private String status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
