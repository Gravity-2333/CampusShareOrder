package com.campusshareorder.backend.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminComplaintDetailVO {
    private Long complaintId;
    private String complaintNo;
    private Long orderId;
    private String orderNo;
    private String productName;
    private Long complainantUserId;
    private String complainantNickname;
    private String accusedNickname;
    private String type;
    private String content;
    private String status;
    private String handleResult;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
    private Boolean openedBySystem;
}
