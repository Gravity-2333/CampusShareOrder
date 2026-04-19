package com.campusshareorder.backend.vo.order;

import lombok.Data;

@Data
public class OrderInitiatorInfoVO {
    private Long userId;
    private String nickname;
    private String phone;
}