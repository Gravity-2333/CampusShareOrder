package com.campusshareorder.backend.vo.complaint;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ComplaintListItemVO {
    private Long complaintId;
    private String complaintNo;
    private String productName;
    private String accusedNickname;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}