package com.campusshareorder.backend.common.enums;

import lombok.Getter;

@Getter
public enum CapitalType {
    PAY("PAY", "支付"),
    REFUND("REFUND", "退款"),
    SETTLE_TO_CREATOR("SETTLE_TO_CREATOR", "结算给发起人");

    private final String code;
    private final String desc;

    CapitalType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
