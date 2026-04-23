package com.campusshareorder.backend.vo.auth;

import lombok.Data;

@Data
public class RegisterResultVO {
    private Long userId;
    private String phone;
    private String nickname;
    private String role;
    private String status;
    private Boolean isVerified;
    private Integer creditScore;
}
