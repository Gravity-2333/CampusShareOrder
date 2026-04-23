package com.campusshareorder.backend.vo.user;

import lombok.Data;

@Data
public class VerifyStudentVO {
    private Boolean isVerified;
    private String studentNo;
}
