package com.campusshareorder.backend.vo.auth;

import lombok.Data;

@Data
public class CurrentLoginInfoVO {
    private String role;
    private Long userId;
    private Long adminId;
    private String phone;
    private String nickname;
    private String studentNo;
    private Boolean isVerified;
    private String status;
    private Integer creditScore;
    private String contactInfo;
    private String username;
}
