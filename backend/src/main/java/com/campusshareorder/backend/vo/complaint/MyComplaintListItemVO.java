package com.campusshareorder.backend.vo.complaint;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MyComplaintListItemVO {
    private Long complaintId;
    private String complaintNo;
    private Long orderId;
    private String orderNo;
    private String productName;
    private String accusedNickname;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}