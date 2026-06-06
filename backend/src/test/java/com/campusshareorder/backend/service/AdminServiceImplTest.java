package com.campusshareorder.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.dto.admin.BanUserRequest;
import com.campusshareorder.backend.dto.admin.CancelOrderRequest;
import com.campusshareorder.backend.dto.admin.HandleComplaintRequest;
import com.campusshareorder.backend.entity.CapitalRecord;
import com.campusshareorder.backend.entity.Complaint;
import com.campusshareorder.backend.entity.CreditChangeRecord;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.entity.GroupOrderMember;
import com.campusshareorder.backend.entity.OperationLog;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.AdminAccountMapper;
import com.campusshareorder.backend.mapper.CapitalRecordMapper;
import com.campusshareorder.backend.mapper.ComplaintMapper;
import com.campusshareorder.backend.mapper.CreditChangeRecordMapper;
import com.campusshareorder.backend.mapper.GroupOrderMapper;
import com.campusshareorder.backend.mapper.GroupOrderMemberMapper;
import com.campusshareorder.backend.mapper.NotificationMapper;
import com.campusshareorder.backend.mapper.OperationLogMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AdminAccountMapper adminAccountMapper;
    @Mock
    private CapitalRecordMapper capitalRecordMapper;
    @Mock
    private ComplaintMapper complaintMapper;
    @Mock
    private CreditChangeRecordMapper creditChangeRecordMapper;
    @Mock
    private GroupOrderMapper groupOrderMapper;
    @Mock
    private GroupOrderMemberMapper groupOrderMemberMapper;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private OperationLogMapper operationLogMapper;
    @Mock
    private OrderService orderService;
    @Mock
    private UserAccountMapper userAccountMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void handleComplaintRejectsAlreadyProcessedComplaint() {
        Complaint complaint = complaint("PROCESSED");
        when(complaintMapper.selectById(1L)).thenReturn(complaint);

        assertThatThrownBy(() -> adminService.handleComplaint(1L, new HandleComplaintRequest(), 900L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.COMPLAINT_ALREADY_PROCESSED.getCode()));

        verify(complaintMapper, never()).updateById(any(Complaint.class));
    }

    @Test
    void handleComplaintRejectsBlankResult() {
        Complaint complaint = complaint("PENDING");
        HandleComplaintRequest request = new HandleComplaintRequest();
        request.setHandleResult("   ");
        when(complaintMapper.selectById(1L)).thenReturn(complaint);

        assertThatThrownBy(() -> adminService.handleComplaint(1L, request, 900L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.getCode()));

        verify(complaintMapper, never()).updateById(any(Complaint.class));
    }

    @Test
    void cancelOrderRejectsBlankReason() {
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason(" ");

        assertThatThrownBy(() -> adminService.cancelOrder(10L, request, 900L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.getCode()));

        verify(groupOrderMapper, never()).updateById(any(GroupOrder.class));
    }

    @Test
    void banUserRejectsBlankReason() {
        BanUserRequest request = new BanUserRequest();
        request.setReason("");

        assertThatThrownBy(() -> adminService.banUser(100L, request, 900L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.getCode()));

        verify(userAccountMapper, never()).updateById(any(UserAccount.class));
    }

    @Test
    void getComplaintDetailIncludesComplainantAndAccusedUserInfo() {
        Complaint complaint = complaint("PENDING");
        GroupOrder order = new GroupOrder();
        order.setId(10L);
        order.setOrderNo("ORD001");
        order.setProductName("测试商品");
        UserAccount complainant = new UserAccount();
        complainant.setId(101L);
        complainant.setNickname("投诉用户");
        UserAccount accused = new UserAccount();
        accused.setId(100L);
        accused.setNickname("被投诉用户");

        when(complaintMapper.selectById(1L)).thenReturn(complaint);
        when(groupOrderMapper.selectById(10L)).thenReturn(order);
        when(userAccountMapper.selectById(101L)).thenReturn(complainant);
        when(userAccountMapper.selectById(100L)).thenReturn(accused);

        var detail = adminService.getComplaintDetail(1L);

        assertThat(detail.getComplainantUserId()).isEqualTo(101L);
        assertThat(detail.getComplainantNickname()).isEqualTo("投诉用户");
        assertThat(detail.getAccusedUserId()).isEqualTo(100L);
        assertThat(detail.getAccusedNickname()).isEqualTo("被投诉用户");
        assertThat(detail.getOrderNo()).isEqualTo("ORD001");
    }

    @Test
    void handleComplaintCancelsOrderRefundsAndAppliesCreditPenalty() {
        Complaint complaint = complaint("PENDING");
        GroupOrder order = new GroupOrder();
        order.setId(10L);
        order.setStatus("WAIT_DELIVERY");
        GroupOrderMember member = new GroupOrderMember();
        member.setId(20L);
        member.setUserId(101L);
        member.setJoinStatus("ACTIVE");
        member.setPayStatus("PAID");
        member.setPayAmount(new BigDecimal("30.00"));
        member.setRefundAmountTotal(BigDecimal.ZERO);
        UserAccount accused = new UserAccount();
        accused.setId(100L);
        accused.setCreditScore(90);

        when(complaintMapper.selectById(1L)).thenReturn(complaint);
        when(groupOrderMapper.selectById(10L)).thenReturn(order);
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(member));
        when(capitalRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(creditChangeRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userAccountMapper.selectById(100L)).thenReturn(accused);

        HandleComplaintRequest request = new HandleComplaintRequest();
        request.setResult("CONFIRMED");
        request.setHandleResult("投诉成立，订单取消");
        adminService.handleComplaint(1L, request, 900L);

        assertThat(complaint.getStatus()).isEqualTo("PROCESSED");
        assertThat(order.getStatus()).isEqualTo("CANCELED");
        assertThat(member.getJoinStatus()).isEqualTo("CANCELED");
        assertThat(member.getPayStatus()).isEqualTo("REFUNDED");
        assertThat(member.getRefundAmountTotal()).isEqualByComparingTo("30.00");
        assertThat(accused.getCreditScore()).isEqualTo(80);

        ArgumentCaptor<CapitalRecord> capitalCaptor = ArgumentCaptor.forClass(CapitalRecord.class);
        verify(capitalRecordMapper).insert(capitalCaptor.capture());
        assertThat(capitalCaptor.getValue().getType()).isEqualTo("REFUND_CANCEL");

        ArgumentCaptor<CreditChangeRecord> creditCaptor = ArgumentCaptor.forClass(CreditChangeRecord.class);
        verify(creditChangeRecordMapper).insert(creditCaptor.capture());
        assertThat(creditCaptor.getValue().getReasonType()).isEqualTo("COMPLAINT_CONFIRMED");
        assertThat(creditCaptor.getValue().getChangeValue()).isEqualTo(-10);
    }

    @Test
    void handleComplaintRejectedDoesNotCancelOrderRefundOrApplyCreditPenalty() {
        Complaint complaint = complaint("PENDING");
        GroupOrder order = new GroupOrder();
        order.setId(10L);
        order.setStatus("WAIT_DELIVERY");

        when(complaintMapper.selectById(1L)).thenReturn(complaint);
        when(groupOrderMapper.selectById(10L)).thenReturn(order);

        HandleComplaintRequest request = new HandleComplaintRequest();
        request.setResult("REJECTED");
        request.setHandleResult("证据不足，投诉驳回");
        adminService.handleComplaint(1L, request, 900L);

        assertThat(complaint.getStatus()).isEqualTo("PROCESSED");
        assertThat(complaint.getHandleResult()).isEqualTo("证据不足，投诉驳回");
        assertThat(order.getStatus()).isEqualTo("WAIT_DELIVERY");

        verify(groupOrderMapper, never()).updateById(any(GroupOrder.class));
        verify(groupOrderMemberMapper, never()).selectList(any(LambdaQueryWrapper.class));
        verify(capitalRecordMapper, never()).insert(any(CapitalRecord.class));
        verify(creditChangeRecordMapper, never()).insert(any(CreditChangeRecord.class));

        ArgumentCaptor<OperationLog> logCaptor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getAction()).isEqualTo("COMPLAINT_REJECTED");
    }

    private Complaint complaint(String status) {
        Complaint complaint = new Complaint();
        complaint.setId(1L);
        complaint.setComplaintNo("CP20260424001");
        complaint.setGroupOrderId(10L);
        complaint.setComplainantUserId(101L);
        complaint.setAccusedUserId(100L);
        complaint.setType("NOT_PURCHASED");
        complaint.setContent("发起人未按时上传凭证");
        complaint.setStatus(status);
        complaint.setCreatedAt(LocalDateTime.now());
        return complaint;
    }
}
