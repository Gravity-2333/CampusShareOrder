package com.campusshareorder.backend.vo.auth;

import lombok.Data;

@Data
public class AdminLoginInfoVO {
    private Long adminId;
    private String username;
    private String role;
    private String status;
}
