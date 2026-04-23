package com.campusshareorder.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_account")
public class UserAccount {
    @TableId
    private Long id;
    private String phone;
    private String passwordHash;
    private String nickname;
    private String studentNo;
    private Boolean isVerified;
    private Integer creditScore;
    private String status;
    private String contactInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}