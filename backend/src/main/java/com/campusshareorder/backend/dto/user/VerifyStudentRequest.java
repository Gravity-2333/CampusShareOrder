package com.campusshareorder.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyStudentRequest {
    @NotBlank(message = "学号不能为空")
    private String studentNo;
}
