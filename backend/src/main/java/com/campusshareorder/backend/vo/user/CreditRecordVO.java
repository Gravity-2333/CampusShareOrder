package com.campusshareorder.backend.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreditRecordVO {
    private Long recordId;
    private Integer changeValue;
    private Integer currentScore;
    private String reasonType;
    private String remark;
    private Long relatedOrderId;
    private Long relatedComplaintId;
    private LocalDateTime createdAt;
}