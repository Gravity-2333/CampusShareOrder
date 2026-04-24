package com.campusshareorder.backend.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("ACTIVE", "正常"),
    BANNED("BANNED", "已封禁");

    private final String code;
    private final String desc;

    UserStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
