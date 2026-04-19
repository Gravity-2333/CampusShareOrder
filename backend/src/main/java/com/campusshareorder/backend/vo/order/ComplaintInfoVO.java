package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComplaintInfoVO {
    private Boolean complaintOpened;
    private Long complaintId;
    private String complaintNo;
    private String type;
    private String status;
    private LocalDateTime created_at;
}