package com.campusshareorder.backend.common.enums;

import lombok.Getter;

@Getter
public enum PayStatus {
    UNPAID("UNPAID", "未支付"),
    PAID("PAID", "已支付"),
    REFUNDED("REFUNDED", "已退款");

    private final String code;
    private final String desc;

    PayStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
