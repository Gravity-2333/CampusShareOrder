package com.campusshareorder.backend.vo.complaint;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ComplaintDetailVO {
    private Long id;
    private String complaintNo;
    private Long orderId;
    private String orderNo;
    private String accusedUserName;
    private String type;
    private String content;
    private String status;
    private String handleResult;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}