package com.campusshareorder.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HandleComplaintRequest {
    @NotBlank(message = "处理结果不能为空")
    @Size(max = 500, message = "处理结果长度不能超过500")
    private String handleResult;
}
