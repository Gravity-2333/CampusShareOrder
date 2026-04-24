package com.campusshareorder.backend.common.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    USER("USER", "普通用户"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String desc;

    RoleType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
