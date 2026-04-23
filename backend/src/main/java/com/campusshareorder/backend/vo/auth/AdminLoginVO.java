package com.campusshareorder.backend.vo.auth;

import lombok.Data;

@Data
public class AdminLoginVO {
    private String token;
    private AdminLoginInfoVO adminInfo;
}
