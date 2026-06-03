package com.campusshareorder.backend.dto.complaint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateComplaintRequest {
    @NotNull(message = "订单ID不能为空")
    @Positive(message = "订单ID必须大于0")
    private Long orderId;

    private Long accusedUserId;

    @NotBlank(message = "投诉类型不能为空")
    @Pattern(regexp = "NOT_PURCHASED|FAKE_RECEIPT|NO_SHIP|NO_DELIVERY|QUALITY|OTHER", message = "投诉类型不合法")
    private String type;

    @NotBlank(message = "投诉内容不能为空")
    @Size(max = 500, message = "投诉内容长度不能超过500")
    private String content;
}
