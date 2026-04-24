package com.campusshareorder.backend.common.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    OPEN("OPEN", "待成团"),
    GROUPED("GROUPED", "已成团"),
    WAIT_DELIVERY("WAIT_DELIVERY", "待发货"),
    WAIT_RECEIVE("WAIT_RECEIVE", "待收货"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELED("CANCELED", "已取消");

    private final String code;
    private final String desc;

    OrderStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
