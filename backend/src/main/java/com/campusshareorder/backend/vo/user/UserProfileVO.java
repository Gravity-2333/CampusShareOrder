package com.campusshareorder.backend.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileVO {
    private Long userId;
    private String phone;
    private String nickname;
    private String studentNo;
    private Boolean isVerified;
    private Integer creditScore;
    private String status;
    private String contactInfo;
    private LocalDateTime createdAt;
}