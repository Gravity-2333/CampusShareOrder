package com.campusshareorder.backend.dto.order;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinOrderRequest {
    @Size(max = 100, message = "加入备注长度不能超过100")
    private String remark;
}
