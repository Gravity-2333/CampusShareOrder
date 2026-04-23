package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReceiveInfoVO {
    private LocalDateTime deliveredAt;
    private LocalDateTime autoConfirmDeadlineAt;
    private Integer totalMembers;
    private Integer receivedMembers;
    private List<OrderMemberVO> receiveStatusSummary;
}
