package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.util.List;

@Data
public class OrderDetailVO {
    private String viewerRoleInOrder;
    private OrderBasicInfoVO basicInfo;
    private OrderInitiatorInfoVO initiatorInfo;
    private OrderMemberVO currentUserMember;
    private List<OrderMemberVO> memberList;
    private PaymentSummaryVO paymentSummary;
    private ReceiptInfoVO receiptInfo;
    private ReceiveInfoVO receiveInfo;
    private List<TimelineItemVO> timeline;
    private ComplaintInfoVO complaintInfo;
    private ActionFlagsVO actionFlags;
}