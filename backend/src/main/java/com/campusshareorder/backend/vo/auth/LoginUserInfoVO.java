package com.campusshareorder.backend.vo.auth;

import lombok.Data;

@Data
public class LoginUserInfoVO {
    private Long userId;
    private String phone;
    private String nickname;
    private String studentNo;
    private Boolean isVerified;
    private String role;
    private String status;
    private Integer creditScore;
    private String contactInfo;
}
