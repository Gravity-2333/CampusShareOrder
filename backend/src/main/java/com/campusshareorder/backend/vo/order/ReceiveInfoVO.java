package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReceiveInfoVO {
    private LocalDateTime deliveredAt;
    private Integer totalMembers;
    private Integer receivedMembers;
}