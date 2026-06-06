package com.campusshareorder.backend.vo.complaint;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ComplaintDetailVO {
    private Long complaintId;
    private String complaintNo;
    private Long orderId;
    private Long complainantUserId;
    private String complainantNickname;
    private Long accusedUserId;
    private String productName;
    private String orderNo;
    private String accusedNickname;
    private String type;
    private String content;
    private String status;
    private Boolean openedBySystem;
    private String handleResult;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
