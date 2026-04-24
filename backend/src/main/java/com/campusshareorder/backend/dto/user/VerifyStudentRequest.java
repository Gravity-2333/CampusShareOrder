package com.campusshareorder.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyStudentRequest {
    @NotBlank(message = "学号不能为空")
    @Size(max = 10, message = "学号长度不能超过10位")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "学号只能包含字母和数字")
    private String studentNo;
}
