package com.campusshareorder.backend.dto.order;

import lombok.Data;

@Data
public class OrderQueryRequest {
    private String keyword;
    private String status;
    private Integer page = 1;
    private Integer pageSize = 10;

    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
