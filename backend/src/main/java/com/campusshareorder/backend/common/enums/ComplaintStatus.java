package com.campusshareorder.backend.common.enums;

import lombok.Getter;

@Getter
public enum ComplaintStatus {
    PENDING("PENDING", "待处理"),
    RESOLVED("RESOLVED", "已解决"),
    REJECTED("REJECTED", "已驳回");

    private final String code;
    private final String desc;

    ComplaintStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
