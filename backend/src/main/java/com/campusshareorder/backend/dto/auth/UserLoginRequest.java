package com.campusshareorder.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;
    @NotBlank(message = "密码不能为空")
    private String password;

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }
}
