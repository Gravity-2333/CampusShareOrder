package com.campusshareorder.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("complaint")
public class Complaint {
    @TableId
    private Long id;
    private String complaintNo;
    private Long groupOrderId;
    private Long complainantUserId;
    private Long accusedUserId;
    private String type;
    private String content;
    private String status;
    private String handleResult;
    private Long handledByAdminId;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}