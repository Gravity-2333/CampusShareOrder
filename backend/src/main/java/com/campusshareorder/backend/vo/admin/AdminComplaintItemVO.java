package com.campusshareorder.backend.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminComplaintItemVO {
    private Long complaintId;
    private String complaintNo;
    private String type;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private String orderNo;
    private String productName;
    private String complainantNickname;
}
