package com.campusshareorder.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HandleComplaintRequest {
    @Pattern(regexp = "CONFIRMED|REJECTED", message = "投诉处理结论不合法")
    private String result = "CONFIRMED";

    @NotBlank(message = "处理结果不能为空")
    @Size(max = 500, message = "处理结果长度不能超过500")
    private String handleResult;

    public String getResult() {
        return result == null || result.isBlank() ? "CONFIRMED" : result.trim();
    }

    public void setResult(String result) {
        this.result = result == null || result.isBlank() ? "CONFIRMED" : result.trim();
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult == null ? null : handleResult.trim();
    }
}
