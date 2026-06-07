package com.campusshareorder.backend.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 50, message = "商品名称长度不能超过50")
    private String productName;

    @Size(max = 255, message = "商品描述长度不能超过255")
    private String productDesc;

    @NotNull(message = "拼单人数不能为空")
    @Min(value = 2, message = "拼单人数不能少于2人")
    @Max(value = 20, message = "拼单人数不能超过20人")
    private Integer totalMemberCount;

    @NotNull(message = "预计总金额不能为空")
    @DecimalMin(value = "0.01", message = "预计总金额必须大于0")
    private BigDecimal estimatedTotalAmount;

    @NotBlank(message = "取货点不能为空")
    @Size(max = 100, message = "取货点长度不能超过100")
    private String pickupPoint;

    @NotBlank(message = "截止时间不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "截止时间格式应为 yyyy-MM-dd HH:mm:ss")
    private String deadlineAt;

    public void setProductName(String productName) {
        this.productName = productName == null ? null : productName.trim();
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc == null ? null : productDesc.trim();
    }

    public void setPickupPoint(String pickupPoint) {
        this.pickupPoint = pickupPoint == null ? null : pickupPoint.trim();
    }

    public void setDeadlineAt(String deadlineAt) {
        this.deadlineAt = deadlineAt == null ? null : deadlineAt.trim();
    }
}
