package com.campusshareorder.backend.common.enums;

import lombok.Getter;

@Getter
public enum MemberJoinStatus {
    ACTIVE("ACTIVE", "有效"),
    EXITED("EXITED", "已退出");

    private final String code;
    private final String desc;

    MemberJoinStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
