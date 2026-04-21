package com.campusshareorder.backend.vo.complaint;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ComplaintListItemVO {
    private Long id;
    private String complaintNo;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}