package com.campusshareorder.backend.common.enums;

import lombok.Getter;

@Getter
public enum ReceiveStatus {
    PENDING("PENDING", "待收货"),
    RECEIVED("RECEIVED", "已收货");

    private final String code;
    private final String desc;

    ReceiveStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
