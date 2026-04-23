package com.campusshareorder.backend.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminUserDetailVO {
    private Long userId;
    private String phone;
    private String nickname;
    private String studentNo;
    private Boolean isVerified;
    private Integer creditScore;
    private String status;
    private String contactInfo;
    private LocalDateTime createdAt;
    private List<AdminCreditRecordVO> creditRecords;
}
