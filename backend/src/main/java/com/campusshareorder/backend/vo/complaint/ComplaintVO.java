package com.campusshareorder.backend.vo.complaint;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComplaintVO {
    private Long complaintId;
    private String complaintNo;
    private Long orderId;
    private String orderNo;
    private String productName;
    private Long complainantUserId;
    private String complainantNickname;
    private Long accusedUserId;
    private String accusedNickname;
    private String type;
    private String content;
    private String status;
    private String handleResult;
    private String handledByAdminNickname;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}