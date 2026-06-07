package com.campusshareorder.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.dto.order.CreateOrderRequest;
import com.campusshareorder.backend.dto.order.OrderQueryRequest;
import com.campusshareorder.backend.dto.order.UploadReceiptRequest;
import com.campusshareorder.backend.entity.CapitalRecord;
import com.campusshareorder.backend.entity.CreditChangeRecord;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.entity.GroupOrderMember;
import com.campusshareorder.backend.entity.OperationLog;
import com.campusshareorder.backend.entity.OrderReceipt;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.CapitalRecordMapper;
import com.campusshareorder.backend.mapper.ComplaintMapper;
import com.campusshareorder.backend.mapper.CreditChangeRecordMapper;
import com.campusshareorder.backend.mapper.GroupOrderMapper;
import com.campusshareorder.backend.mapper.GroupOrderMemberMapper;
import com.campusshareorder.backend.mapper.NotificationMapper;
import com.campusshareorder.backend.mapper.OperationLogMapper;
import com.campusshareorder.backend.mapper.OrderReceiptMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.impl.OrderServiceImpl;
import com.campusshareorder.backend.service.ReceiptStorageService;
import com.campusshareorder.backend.vo.order.OrderDetailVO;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private GroupOrderMapper groupOrderMapper;
    @Mock
    private GroupOrderMemberMapper groupOrderMemberMapper;
    @Mock
    private UserAccountMapper userAccountMapper;
    @Mock
    private OrderReceiptMapper orderReceiptMapper;
    @Mock
    private ComplaintMapper complaintMapper;
    @Mock
    private CapitalRecordMapper capitalRecordMapper;
    @Mock
    private CreditChangeRecordMapper creditChangeRecordMapper;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private OperationLogMapper operationLogMapper;
    @Mock
    private ReceiptStorageService receiptStorageService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void uploadReceiptRefundsActualAmountDifference() {
        GroupOrder order = groupedOrder();
        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(orderReceiptMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                paidMember(11L, 101L, false),
                paidMember(12L, 102L, true)
        ));
        when(capitalRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(receiptStorageService.store(any())).thenReturn("/uploads/receipts/receipt.jpg");

        UploadReceiptRequest request = new UploadReceiptRequest();
        request.setActualTotalAmount(new BigDecimal("54.00"));
        request.setExpectedDeliveryStartAt(LocalDateTime.now().plusMinutes(10));
        request.setExpectedDeliveryEndAt(LocalDateTime.now().plusMinutes(40));

        order.setReceiptUploadDeadlineAt(LocalDateTime.now().plusMinutes(20));
        orderService.uploadReceipt(1L, request, null, 102L);

        ArgumentCaptor<OrderReceipt> receiptCaptor = ArgumentCaptor.forClass(OrderReceipt.class);
        verify(orderReceiptMapper).insert(receiptCaptor.capture());
        assertThat(receiptCaptor.getValue().getActualTotalAmount()).isEqualByComparingTo("54.00");

        assertThat(order.getStatus()).isEqualTo("WAIT_DELIVERY");
        assertThat(order.getActualPerAmount()).isEqualByComparingTo("27.00");
        verify(groupOrderMapper).updateById(order);

        ArgumentCaptor<CapitalRecord> capitalCaptor = ArgumentCaptor.forClass(CapitalRecord.class);
        verify(capitalRecordMapper, atLeastOnce()).insert(capitalCaptor.capture());
        assertThat(capitalCaptor.getAllValues())
                .anySatisfy(record -> {
                    assertThat(record.getType()).isEqualTo("REFUND_DIFF");
                    assertThat(record.getAmount()).isEqualByComparingTo("3.00");
                });
    }

    @Test
    void confirmReceivedCompletesOrderAndSettlesCreator() {
        GroupOrder order = waitReceiveOrder();
        GroupOrderMember currentMember = paidMember(11L, 101L, false);
        currentMember.setReceiveStatus("WAIT_CONFIRM");
        currentMember.setRefundAmountTotal(new BigDecimal("3.00"));
        GroupOrderMember creator = paidMember(12L, 102L, true);
        creator.setReceiveStatus("RECEIVED");
        creator.setRefundAmountTotal(new BigDecimal("3.00"));
        UserAccount memberUser = user(101L, 80);
        UserAccount creatorUser = user(102L, 99);

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(currentMember);
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(currentMember, creator));
        when(capitalRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(creditChangeRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userAccountMapper.selectById(101L)).thenReturn(memberUser);
        when(userAccountMapper.selectById(102L)).thenReturn(creatorUser);

        orderService.confirmReceived(1L, 101L);

        assertThat(currentMember.getReceiveStatus()).isEqualTo("RECEIVED");
        assertThat(order.getStatus()).isEqualTo("COMPLETED");
        assertThat(memberUser.getCreditScore()).isEqualTo(82);
        assertThat(creatorUser.getCreditScore()).isEqualTo(100);

        ArgumentCaptor<CapitalRecord> capitalCaptor = ArgumentCaptor.forClass(CapitalRecord.class);
        verify(capitalRecordMapper).insert(capitalCaptor.capture());
        assertThat(capitalCaptor.getValue().getType()).isEqualTo("SETTLE_TO_CREATOR");
        assertThat(capitalCaptor.getValue().getAmount()).isEqualByComparingTo("54.00");
        assertThat(capitalCaptor.getValue().getOperatorType()).isEqualTo("SYSTEM");

        ArgumentCaptor<CreditChangeRecord> creditCaptor = ArgumentCaptor.forClass(CreditChangeRecord.class);
        verify(creditChangeRecordMapper, atLeastOnce()).insert(creditCaptor.capture());
        assertThat(creditCaptor.getAllValues())
                .hasSize(2)
                .allSatisfy(record -> {
                    assertThat(record.getReasonType()).isEqualTo("ORDER_FINISHED");
                    assertThat(record.getChangeValue()).isEqualTo(2);
                    assertThat(record.getRelatedOrderId()).isEqualTo(1L);
                });

        ArgumentCaptor<OperationLog> logCaptor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogMapper, atLeastOnce()).insert(logCaptor.capture());
        assertThat(logCaptor.getAllValues()).anyMatch(log -> "ORDER_COMPLETED".equals(log.getAction()));
    }

    @Test
    void recoverCompletedOrdersDoesNotDuplicateSettlementRecord() {
        GroupOrder order = waitReceiveOrder();
        GroupOrderMember member = paidMember(11L, 101L, false);
        member.setReceiveStatus("RECEIVED");
        GroupOrderMember creator = paidMember(12L, 102L, true);
        creator.setReceiveStatus("RECEIVED");

        when(groupOrderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(order));
        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(member, creator));
        when(capitalRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        orderService.recoverCompletedOrders();

        assertThat(order.getStatus()).isEqualTo("COMPLETED");
        verify(capitalRecordMapper, never()).insert(any(CapitalRecord.class));
    }

    @Test
    void uploadReceiptRejectsActualAmountGreaterThanEstimatedAmount() {
        GroupOrder order = groupedOrder();
        order.setReceiptUploadDeadlineAt(LocalDateTime.now().plusMinutes(20));
        when(groupOrderMapper.selectById(1L)).thenReturn(order);

        UploadReceiptRequest request = new UploadReceiptRequest();
        request.setActualTotalAmount(new BigDecimal("61.00"));
        request.setExpectedDeliveryStartAt(LocalDateTime.now().plusMinutes(10));
        request.setExpectedDeliveryEndAt(LocalDateTime.now().plusMinutes(40));

        assertThatThrownBy(() -> orderService.uploadReceipt(1L, request, null, 102L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.getCode()))
                .hasMessageContaining("实际总金额");

        verify(orderReceiptMapper, never()).insert(any(OrderReceipt.class));
        verify(receiptStorageService, never()).store(any());
    }

    @Test
    void createOrderRejectsInvalidDeadlineValueAsValidationError() {
        UserAccount user = user(101L, 80);
        user.setIsVerified(true);
        when(userAccountMapper.selectById(101L)).thenReturn(user);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductName("测试拼单");
        request.setProductDesc("测试描述");
        request.setTotalMemberCount(2);
        request.setEstimatedTotalAmount(new BigDecimal("20.00"));
        request.setPickupPoint("图书馆门口");
        request.setDeadlineAt("2026-99-99 10:00:00");

        assertThatThrownBy(() -> orderService.createOrder(request, 101L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.getCode()))
                .hasMessageContaining("截止时间格式");

        verify(groupOrderMapper, never()).insert(any(GroupOrder.class));
    }

    @Test
    void getOrdersClampsNegativeRemainingCountToZero() {
        GroupOrder order = groupedOrder();
        order.setTotalMemberCount(2);
        order.setCurrentMemberCount(5);
        Page<GroupOrder> orderPage = new Page<>(1, 10);
        orderPage.setRecords(List.of(order));
        orderPage.setTotal(1);
        when(groupOrderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(orderPage);

        var result = orderService.getOrderList(new OrderQueryRequest());

        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getRemainingCount()).isZero();
    }

    @Test
    void createOrderTrimsTextFieldsBeforePersisting() {
        UserAccount user = user(101L, 80);
        user.setIsVerified(true);
        when(userAccountMapper.selectById(101L)).thenReturn(user);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductName("  测试拼单  ");
        request.setProductDesc("  测试描述  ");
        request.setTotalMemberCount(2);
        request.setEstimatedTotalAmount(new BigDecimal("20.00"));
        request.setPickupPoint("  图书馆门口  ");
        request.setDeadlineAt(LocalDateTime.now().plusHours(2).withNano(0).toString().replace('T', ' '));

        orderService.createOrder(request, 101L);

        ArgumentCaptor<GroupOrder> orderCaptor = ArgumentCaptor.forClass(GroupOrder.class);
        verify(groupOrderMapper).insert(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getProductName()).isEqualTo("测试拼单");
        assertThat(orderCaptor.getValue().getProductDesc()).isEqualTo("测试描述");
        assertThat(orderCaptor.getValue().getPickupPoint()).isEqualTo("图书馆门口");
    }

    @Test
    void uploadReceiptRejectsPastExpectedDeliveryStartTime() {
        GroupOrder order = groupedOrder();
        order.setReceiptUploadDeadlineAt(LocalDateTime.now().plusMinutes(20));
        when(groupOrderMapper.selectById(1L)).thenReturn(order);

        UploadReceiptRequest request = new UploadReceiptRequest();
        request.setActualTotalAmount(new BigDecimal("54.00"));
        request.setExpectedDeliveryStartAt(LocalDateTime.now().minusMinutes(1));
        request.setExpectedDeliveryEndAt(LocalDateTime.now().plusMinutes(40));

        assertThatThrownBy(() -> orderService.uploadReceipt(1L, request, null, 102L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.getCode()))
                .hasMessageContaining("开始送达时间");

        verify(orderReceiptMapper, never()).insert(any(OrderReceipt.class));
        verify(receiptStorageService, never()).store(any());
    }

    @Test
    void cancelExpiredOpenOrdersCancelsMembersAndRefundsPaidAmount() {
        GroupOrder order = groupedOrder();
        order.setStatus("OPEN");
        GroupOrderMember paidMember = paidMember(11L, 101L, false);

        when(groupOrderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(order));
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(paidMember));
        when(capitalRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        orderService.cancelExpiredOpenOrders();

        assertThat(order.getStatus()).isEqualTo("CANCELED");
        assertThat(order.getCancelReason()).isEqualTo("DEADLINE_NOT_GROUPED");
        assertThat(paidMember.getJoinStatus()).isEqualTo("CANCELED");
        assertThat(paidMember.getPayStatus()).isEqualTo("REFUNDED");
        assertThat(paidMember.getRefundAmountTotal()).isEqualByComparingTo("30.00");

        ArgumentCaptor<CapitalRecord> capitalCaptor = ArgumentCaptor.forClass(CapitalRecord.class);
        verify(capitalRecordMapper).insert(capitalCaptor.capture());
        assertThat(capitalCaptor.getValue().getType()).isEqualTo("REFUND_CANCEL");
    }

    @Test
    void paidMemberCanExitOpenOrderAndRefundsPaidAmount() {
        GroupOrder order = groupedOrder();
        order.setStatus("OPEN");
        order.setCurrentMemberCount(2);
        GroupOrderMember paidMember = paidMember(11L, 101L, false);

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(paidMember);
        when(capitalRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        orderService.exitOrder(1L, 101L);

        assertThat(paidMember.getJoinStatus()).isEqualTo("REFUNDED");
        assertThat(paidMember.getPayStatus()).isEqualTo("REFUNDED");
        assertThat(paidMember.getRefundAmountTotal()).isEqualByComparingTo("30.00");
        assertThat(order.getCurrentMemberCount()).isEqualTo(1);

        ArgumentCaptor<CapitalRecord> capitalCaptor = ArgumentCaptor.forClass(CapitalRecord.class);
        verify(capitalRecordMapper).insert(capitalCaptor.capture());
        assertThat(capitalCaptor.getValue().getType()).isEqualTo("REFUND_EXIT");
        assertThat(capitalCaptor.getValue().getAmount()).isEqualByComparingTo("30.00");
    }

    @Test
    void exitedMemberCanRejoinOpenOrder() {
        GroupOrder order = groupedOrder();
        order.setStatus("OPEN");
        order.setCurrentMemberCount(1);
        GroupOrderMember exitedMember = paidMember(11L, 101L, false);
        exitedMember.setJoinStatus("REFUNDED");
        exitedMember.setPayStatus("REFUNDED");
        exitedMember.setPaidAt(LocalDateTime.now().minusMinutes(5));

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(exitedMember);
        when(userAccountMapper.selectById(101L)).thenReturn(user(101L, 80));

        orderService.joinOrder(1L, null, 101L);

        assertThat(exitedMember.getJoinStatus()).isEqualTo("ACTIVE");
        assertThat(exitedMember.getPayStatus()).isEqualTo("UNPAID");
        assertThat(exitedMember.getPaidAt()).isNull();
        assertThat(exitedMember.getRefundAmountTotal()).isEqualByComparingTo("0.00");
        assertThat(order.getCurrentMemberCount()).isEqualTo(2);
        verify(groupOrderMemberMapper).updateById(exitedMember);
        verify(groupOrderMapper).updateById(order);
    }

    @Test
    void unpaidExitedMemberCanRejoinOpenOrder() {
        GroupOrder order = groupedOrder();
        order.setStatus("OPEN");
        order.setCurrentMemberCount(1);
        GroupOrderMember exitedMember = paidMember(11L, 101L, false);
        exitedMember.setJoinStatus("EXITED");
        exitedMember.setPayStatus("UNPAID");
        exitedMember.setPaidAt(null);

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(exitedMember);
        when(userAccountMapper.selectById(101L)).thenReturn(user(101L, 80));

        orderService.joinOrder(1L, null, 101L);

        assertThat(exitedMember.getJoinStatus()).isEqualTo("ACTIVE");
        assertThat(exitedMember.getPayStatus()).isEqualTo("UNPAID");
        assertThat(exitedMember.getPaidAt()).isNull();
        assertThat(exitedMember.getRefundAmountTotal()).isEqualByComparingTo("0.00");
        assertThat(order.getCurrentMemberCount()).isEqualTo(2);
        verify(groupOrderMemberMapper).updateById(exitedMember);
        verify(groupOrderMapper).updateById(order);
    }

    @Test
    void joinOrderHandlesMissingCurrentMemberCount() {
        GroupOrder order = groupedOrder();
        order.setStatus("OPEN");
        order.setCurrentMemberCount(null);

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userAccountMapper.selectById(101L)).thenReturn(user(101L, 80));

        orderService.joinOrder(1L, null, 101L);

        assertThat(order.getCurrentMemberCount()).isEqualTo(1);
        verify(groupOrderMemberMapper).insert(any(GroupOrderMember.class));
        verify(groupOrderMapper).updateById(order);
    }

    @Test
    void unverifiedUserCannotJoinOrder() {
        UserAccount user = user(101L, 80);
        user.setIsVerified(false);
        when(userAccountMapper.selectById(101L)).thenReturn(user);

        assertThatThrownBy(() -> orderService.joinOrder(1L, null, 101L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_VERIFIED.getCode()));

        verify(groupOrderMapper, never()).selectById(1L);
        verify(groupOrderMemberMapper, never()).insert(any(GroupOrderMember.class));
    }

    @Test
    void creatorCanCancelOpenOrderWithoutParticipantsAndRefundPaidAmount() {
        GroupOrder order = groupedOrder();
        order.setStatus("OPEN");
        order.setCurrentMemberCount(1);
        GroupOrderMember creator = paidMember(12L, 102L, true);

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(creator));
        when(capitalRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        orderService.cancelOrder(1L, 102L);

        assertThat(order.getStatus()).isEqualTo("CANCELED");
        assertThat(order.getCancelReason()).isEqualTo("CREATOR_CANCELED");
        assertThat(creator.getPayStatus()).isEqualTo("REFUNDED");
        assertThat(creator.getRefundAmountTotal()).isEqualByComparingTo("30.00");

        ArgumentCaptor<CapitalRecord> capitalCaptor = ArgumentCaptor.forClass(CapitalRecord.class);
        verify(capitalRecordMapper).insert(capitalCaptor.capture());
        assertThat(capitalCaptor.getValue().getType()).isEqualTo("REFUND_CANCEL");
    }

    @Test
    void creatorCannotCancelOpenOrderWithParticipant() {
        GroupOrder order = groupedOrder();
        order.setStatus("OPEN");
        order.setCurrentMemberCount(2);

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                paidMember(12L, 102L, true),
                paidMember(11L, 101L, false)
        ));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 102L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已有参与者");

        verify(groupOrderMapper, never()).updateById(any(GroupOrder.class));
        verify(capitalRecordMapper, never()).insert(any(CapitalRecord.class));
    }

    @Test
    void exitedMemberCannotPayOpenOrder() {
        GroupOrder order = groupedOrder();
        order.setStatus("OPEN");
        GroupOrderMember exitedMember = paidMember(11L, 101L, false);
        exitedMember.setJoinStatus("EXITED");
        exitedMember.setPayStatus("UNPAID");

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(exitedMember);
        when(userAccountMapper.selectById(101L)).thenReturn(user(101L, 80));

        assertThatThrownBy(() -> orderService.payOrder(1L, 101L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.ORDER_STATUS_INVALID.getCode()));

        verify(groupOrderMemberMapper, never()).updateById(any(GroupOrderMember.class));
        verify(capitalRecordMapper, never()).insert(any(CapitalRecord.class));
    }

    @Test
    void exitedMemberCannotConfirmReceived() {
        GroupOrder order = waitReceiveOrder();
        GroupOrderMember exitedMember = paidMember(11L, 101L, false);
        exitedMember.setJoinStatus("EXITED");
        exitedMember.setReceiveStatus("WAIT_CONFIRM");

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(exitedMember);

        assertThatThrownBy(() -> orderService.confirmReceived(1L, 101L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.ORDER_STATUS_INVALID.getCode()));

        verify(groupOrderMemberMapper, never()).updateById(any(GroupOrderMember.class));
    }

    @Test
    void fullOpenOrderDetailDoesNotExposeJoinAction() {
        GroupOrder order = groupedOrder();
        order.setStatus("OPEN");
        order.setCurrentMemberCount(2);
        GroupOrderMember creator = paidMember(12L, 102L, true);
        GroupOrderMember member = paidMember(11L, 101L, false);

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(creator, member));
        when(complaintMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(orderReceiptMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userAccountMapper.selectById(any())).thenAnswer(invocation -> user(invocation.getArgument(0), 80));

        OrderDetailVO detail = orderService.getOrderDetail(1L, 999L);

        assertThat(detail.getActionFlags().isCanJoin()).isFalse();
    }

    @Test
    void exitedMemberDetailExposesRejoinAction() {
        GroupOrder order = groupedOrder();
        order.setStatus("OPEN");
        order.setCurrentMemberCount(1);
        GroupOrderMember exitedMember = paidMember(11L, 101L, false);
        exitedMember.setJoinStatus("REFUNDED");
        exitedMember.setPayStatus("REFUNDED");

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(exitedMember));
        when(complaintMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(orderReceiptMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userAccountMapper.selectById(any())).thenAnswer(invocation -> user(invocation.getArgument(0), 80));

        OrderDetailVO detail = orderService.getOrderDetail(1L, 101L);

        assertThat(detail.getActionFlags().isCanJoin()).isTrue();
        assertThat(detail.getCurrentUserMember()).isNotNull();
        assertThat(detail.getMemberList()).isEmpty();
    }

    @Test
    void inactiveMemberDetailDoesNotExposeComplaintAction() {
        GroupOrder order = waitReceiveOrder();
        order.setComplaintOpened(true);
        GroupOrderMember exitedMember = paidMember(11L, 101L, false);
        exitedMember.setJoinStatus("EXITED");

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(exitedMember));
        when(complaintMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(orderReceiptMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userAccountMapper.selectById(any())).thenAnswer(invocation -> user(invocation.getArgument(0), 80));

        OrderDetailVO detail = orderService.getOrderDetail(1L, 101L);

        assertThat(detail.getActionFlags().isCanCreateComplaint()).isFalse();
        assertThat(detail.getActionFlags().isCanConfirmReceived()).isFalse();
    }

    @Test
    void canceledOrderTimelineDoesNotShowGroupedEvent() {
        GroupOrder order = groupedOrder();
        order.setStatus("CANCELED");
        GroupOrderMember creator = paidMember(12L, 102L, true);

        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(creator));
        when(complaintMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(orderReceiptMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userAccountMapper.selectById(any())).thenAnswer(invocation -> user(invocation.getArgument(0), 80));

        OrderDetailVO detail = orderService.getOrderDetail(1L, 102L);

        assertThat(detail.getTimeline())
                .extracting("description")
                .contains("订单已取消")
                .doesNotContain("订单已成团");
    }

    @Test
    void openReceiptTimeoutComplaintsOpensComplaintChannelWhenReceiptMissing() {
        GroupOrder order = groupedOrder();
        order.setComplaintOpened(false);
        when(groupOrderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(order));
        when(orderReceiptMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                paidMember(11L, 101L, false),
                paidMember(12L, 102L, true)
        ));

        orderService.openReceiptTimeoutComplaints();

        assertThat(order.getComplaintOpened()).isTrue();
        verify(groupOrderMapper).updateById(order);

        ArgumentCaptor<OperationLog> logCaptor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getAction()).isEqualTo("COMPLAINT_CHANNEL_OPENED");
    }

    @Test
    void autoConfirmReceivedMembersCompletesOrderAfterTimeout() {
        GroupOrder order = waitReceiveOrder();
        order.setDeliveredAt(LocalDateTime.now().minusMinutes(40));
        GroupOrderMember timeoutMember = paidMember(11L, 101L, false);
        timeoutMember.setReceiveStatus("WAIT_CONFIRM");
        GroupOrderMember creator = paidMember(12L, 102L, true);
        creator.setReceiveStatus("RECEIVED");

        when(groupOrderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(order));
        when(groupOrderMemberMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(timeoutMember))
                .thenReturn(List.of(timeoutMember, creator));
        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(capitalRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        orderService.autoConfirmReceivedMembers();

        assertThat(timeoutMember.getReceiveStatus()).isEqualTo("AUTO_RECEIVED");
        assertThat(order.getStatus()).isEqualTo("COMPLETED");

        ArgumentCaptor<CapitalRecord> capitalCaptor = ArgumentCaptor.forClass(CapitalRecord.class);
        verify(capitalRecordMapper).insert(capitalCaptor.capture());
        assertThat(capitalCaptor.getValue().getType()).isEqualTo("SETTLE_TO_CREATOR");
    }

    private GroupOrder groupedOrder() {
        GroupOrder order = new GroupOrder();
        order.setId(1L);
        order.setCreatorUserId(102L);
        order.setStatus("GROUPED");
        order.setTotalMemberCount(2);
        order.setEstimatedTotalAmount(new BigDecimal("60.00"));
        order.setEstimatedPerAmount(new BigDecimal("30.00"));
        return order;
    }

    private GroupOrder waitReceiveOrder() {
        GroupOrder order = groupedOrder();
        order.setStatus("WAIT_RECEIVE");
        order.setActualTotalAmount(new BigDecimal("54.00"));
        return order;
    }

    private GroupOrderMember paidMember(Long memberId, Long userId, boolean creator) {
        GroupOrderMember member = new GroupOrderMember();
        member.setId(memberId);
        member.setGroupOrderId(1L);
        member.setUserId(userId);
        member.setIsCreator(creator);
        member.setJoinStatus("ACTIVE");
        member.setPayStatus("PAID");
        member.setPayAmount(new BigDecimal("30.00"));
        member.setRefundAmountTotal(BigDecimal.ZERO);
        member.setReceiveStatus("WAIT_CONFIRM");
        return member;
    }

    private UserAccount user(Long userId, int creditScore) {
        UserAccount user = new UserAccount();
        user.setId(userId);
        user.setCreditScore(creditScore);
        user.setIsVerified(true);
        return user;
    }
}
