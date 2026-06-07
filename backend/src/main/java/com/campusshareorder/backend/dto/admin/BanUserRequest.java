package com.campusshareorder.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BanUserRequest {
    @NotBlank(message = "封禁原因不能为空")
    @Size(max = 255, message = "封禁原因长度不能超过255")
    private String reason;

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }
}
