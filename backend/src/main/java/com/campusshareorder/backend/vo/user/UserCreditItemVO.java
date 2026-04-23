package com.campusshareorder.backend.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCreditItemVO {
    private Long recordId;
    private Integer delta;
    private Integer currentScore;
    private String changeReason;
    private String reasonType;
    private String remark;
    private Long relatedOrderId;
    private Long relatedComplaintId;
    private LocalDateTime createdAt;
}
