package com.campusshareorder.backend.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminCreditRecordVO {
    private LocalDateTime createdAt;
    private String changeReason;
    private Integer delta;
}
