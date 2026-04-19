package com.campusshareorder.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_account")
public class AdminAccount {
    @TableId
    private Long id;
    private String username;
    private String passwordHash;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}