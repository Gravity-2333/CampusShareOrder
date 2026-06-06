package com.campusshareorder.backend.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserListItemVO {
    private Long userId;
    private String phone;
    private String nickname;
    private String studentNo;
    private Boolean isVerified;
    private Integer creditScore;
    private String status;
    private LocalDateTime createdAt;
}
