package com.campusshareorder.backend.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UploadReceiptRequest {
    @NotBlank(message = "凭证图片不能为空")
    @Size(max = 500, message = "凭证图片地址长度不能超过500")
    private String imageUrl;

    @NotNull(message = "实际总金额不能为空")
    @DecimalMin(value = "0.01", message = "实际总金额必须大于0")
    private BigDecimal actualTotalAmount;

    @NotNull(message = "预计开始送达时间不能为空")
    private LocalDateTime expectedDeliveryStartAt;

    @NotNull(message = "预计最晚送达时间不能为空")
    private LocalDateTime expectedDeliveryEndAt;
}
