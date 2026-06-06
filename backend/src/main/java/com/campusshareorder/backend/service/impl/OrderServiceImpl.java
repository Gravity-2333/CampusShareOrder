package com.campusshareorder.backend.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.dto.order.CreateOrderRequest;
import com.campusshareorder.backend.dto.order.JoinOrderRequest;
import com.campusshareorder.backend.dto.order.MyOrderQueryRequest;
import com.campusshareorder.backend.dto.order.OrderQueryRequest;
import com.campusshareorder.backend.dto.order.UploadReceiptRequest;
import com.campusshareorder.backend.entity.CapitalRecord;
import com.campusshareorder.backend.entity.Complaint;
import com.campusshareorder.backend.entity.CreditChangeRecord;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.entity.GroupOrderMember;
import com.campusshareorder.backend.entity.Notification;
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
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.service.ReceiptStorageService;
import com.campusshareorder.backend.vo.common.PageVO;
import com.campusshareorder.backend.vo.order.ActionFlagsVO;
import com.campusshareorder.backend.vo.order.ComplaintInfoVO;
import com.campusshareorder.backend.vo.order.CreateOrderVO;
import com.campusshareorder.backend.vo.order.MyOrderListItemVO;
import com.campusshareorder.backend.vo.order.OrderBasicInfoVO;
import com.campusshareorder.backend.vo.order.OrderDetailVO;
import com.campusshareorder.backend.vo.order.OrderInitiatorInfoVO;
import com.campusshareorder.backend.vo.order.OrderListItemVO;
import com.campusshareorder.backend.vo.order.OrderMemberVO;
import com.campusshareorder.backend.vo.order.PaymentSummaryVO;
import com.campusshareorder.backend.vo.order.ReceiveInfoVO;
import com.campusshareorder.backend.vo.order.ReceiptInfoVO;
import com.campusshareorder.backend.vo.order.TimelineItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<GroupOrderMapper, GroupOrder> implements OrderService {

    private final GroupOrderMapper groupOrderMapper;
    private final GroupOrderMemberMapper groupOrderMemberMapper;
    private final UserAccountMapper userAccountMapper;
    private final OrderReceiptMapper orderReceiptMapper;
    private final ComplaintMapper complaintMapper;
    private final CapitalRecordMapper capitalRecordMapper;
    private final CreditChangeRecordMapper creditChangeRecordMapper;
    private final NotificationMapper notificationMapper;
    private final OperationLogMapper operationLogMapper;
    private final ReceiptStorageService receiptStorageService;

    @Override
    @Transactional
    public CreateOrderVO createOrder(CreateOrderRequest request, Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BusinessException(ErrorCode.USER_NOT_VERIFIED);
        }

        LocalDateTime deadlineAt;
        try {
            deadlineAt = LocalDateTimeUtil.parse(request.getDeadlineAt(), "yyyy-MM-dd HH:mm:ss");
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "截止时间格式不正确");
        }
        if (!deadlineAt.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "截止时间必须晚于当前时间");
        }

        BigDecimal estimatedPerAmount = request.getEstimatedTotalAmount().divide(
                BigDecimal.valueOf(request.getTotalMemberCount()),
                2,
                RoundingMode.HALF_UP
        );

        String productName = request.getProductName().trim();
        String productDesc = request.getProductDesc() == null ? null : request.getProductDesc().trim();
        String pickupPoint = request.getPickupPoint().trim();

        GroupOrder order = new GroupOrder();
        order.setOrderNo(generateOrderNo());
        order.setCreatorUserId(userId);
        order.setProductName(productName);
        order.setProductDesc(productDesc);
        order.setTotalMemberCount(request.getTotalMemberCount());
        order.setCurrentMemberCount(1);
        order.setEstimatedTotalAmount(request.getEstimatedTotalAmount());
        order.setEstimatedPerAmount(estimatedPerAmount);
        order.setPickupPoint(pickupPoint);
        order.setDeadlineAt(deadlineAt);
        order.setStatus("OPEN");
        order.setComplaintOpened(false);
        groupOrderMapper.insert(order);

        GroupOrderMember creatorMember = new GroupOrderMember();
        creatorMember.setGroupOrderId(order.getId());
        creatorMember.setUserId(userId);
        creatorMember.setIsCreator(true);
        creatorMember.setRole("INITIATOR");
        creatorMember.setJoinStatus("ACTIVE");
        creatorMember.setPayStatus("UNPAID");
        creatorMember.setPayAmount(estimatedPerAmount);
        creatorMember.setRefundAmountTotal(BigDecimal.ZERO);
        creatorMember.setReceiveStatus("NOT_READY");
        groupOrderMemberMapper.insert(creatorMember);
        insertOperationLog("USER", userId, "ORDER", order.getId(), "ORDER_CREATED", productName);

        CreateOrderVO vo = new CreateOrderVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        return vo;
    }

    @Override
    public PageVO<OrderListItemVO> getOrderList(OrderQueryRequest request) {
        Page<GroupOrder> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<GroupOrder> wrapper = new LambdaQueryWrapper<GroupOrder>()
                .ne(GroupOrder::getStatus, "CANCELED");

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String keyword = request.getKeyword().trim();
            wrapper.and(item -> item.like(GroupOrder::getProductName, keyword)
                    .or()
                    .like(GroupOrder::getOrderNo, keyword));
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            wrapper.eq(GroupOrder::getStatus, request.getStatus().trim());
        }
        wrapper.orderByDesc(GroupOrder::getCreatedAt);

        Page<GroupOrder> orderPage = groupOrderMapper.selectPage(page, wrapper);
        List<OrderListItemVO> list = orderPage.getRecords().stream().map(order -> {
            OrderListItemVO vo = new OrderListItemVO();
            vo.setOrderId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setProductName(order.getProductName());
            vo.setProductDesc(order.getProductDesc());
            vo.setTotalMemberCount(order.getTotalMemberCount());
            vo.setCurrentMemberCount(order.getCurrentMemberCount());
            vo.setRemainingCount(Math.max(order.getTotalMemberCount() - order.getCurrentMemberCount(), 0));
            vo.setEstimatedTotalAmount(order.getEstimatedTotalAmount());
            vo.setEstimatedPerAmount(order.getEstimatedPerAmount());
            vo.setPickupPoint(order.getPickupPoint());
            vo.setStatus(order.getStatus());
            vo.setDeadlineAt(order.getDeadlineAt());
            vo.setCreatedAt(order.getCreatedAt());

            UserAccount creator = userAccountMapper.selectById(order.getCreatorUserId());
            vo.setCreatorNickname(creator == null ? "" : creator.getNickname());
            return vo;
        }).collect(Collectors.toList());

        return new PageVO<>(list, orderPage.getTotal(), orderPage.getCurrent(), orderPage.getSize(), orderPage.getPages());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId, Long userId) {
        GroupOrder order = requireOrder(orderId);
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>()
                        .eq(GroupOrderMember::getGroupOrderId, orderId)
                        .orderByAsc(GroupOrderMember::getCreatedAt)
        );
        List<Complaint> complaints = complaintMapper.selectList(
                new LambdaQueryWrapper<Complaint>()
                        .eq(Complaint::getGroupOrderId, orderId)
                        .orderByDesc(Complaint::getCreatedAt)
        );
        OrderReceipt receipt = orderReceiptMapper.selectOne(
                new LambdaQueryWrapper<OrderReceipt>().eq(OrderReceipt::getGroupOrderId, orderId)
        );

        OrderDetailVO detailVO = new OrderDetailVO();
        detailVO.setBasicInfo(buildBasicInfo(order));

        UserAccount creator = userAccountMapper.selectById(order.getCreatorUserId());
        detailVO.setInitiatorInfo(buildInitiatorInfo(creator));

        List<OrderMemberVO> memberVOList = members.stream()
                .filter(member -> "ACTIVE".equals(member.getJoinStatus()))
                .map(this::buildMemberVO)
                .collect(Collectors.toList());
        detailVO.setMemberList(memberVOList);

        GroupOrderMember currentMember = members.stream()
                .filter(item -> Objects.equals(item.getUserId(), userId))
                .findFirst()
                .orElse(null);
        detailVO.setCurrentUserMember(currentMember == null ? null : buildMemberVO(currentMember));

        if (Objects.equals(order.getCreatorUserId(), userId)) {
            detailVO.setViewerRoleInOrder("INITIATOR");
        } else if (currentMember != null) {
            detailVO.setViewerRoleInOrder("MEMBER");
        } else {
            detailVO.setViewerRoleInOrder("VIEWER");
        }

        detailVO.setPaymentSummary(buildPaymentSummary(order, members));
        detailVO.setReceiptInfo(buildReceiptInfo(receipt));
        detailVO.setReceiveInfo(buildReceiveInfo(order, memberVOList));
        detailVO.setComplaintInfo(buildComplaintInfo(order, complaints, userId));
        detailVO.setActionFlags(buildActionFlags(order, currentMember, receipt, complaints, userId));
        detailVO.setTimeline(buildTimeline(order, creator, receipt, complaints, userId));
        return detailVO;
    }

    @Override
    public PageVO<MyOrderListItemVO> getMyOrders(MyOrderQueryRequest request, Long userId) {
        Page<GroupOrderMember> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<GroupOrderMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupOrderMember::getUserId, userId).orderByDesc(GroupOrderMember::getCreatedAt);
        Page<GroupOrderMember> memberPage = groupOrderMemberMapper.selectPage(page, wrapper);

        List<MyOrderListItemVO> list = memberPage.getRecords().stream().map(member -> {
            GroupOrder order = groupOrderMapper.selectById(member.getGroupOrderId());
            if (order == null) {
                return null;
            }

            MyOrderListItemVO vo = new MyOrderListItemVO();
            vo.setOrderId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setProductName(order.getProductName());
            vo.setPickupPoint(order.getPickupPoint());
            vo.setStatus(order.getStatus());
            vo.setMyRole(Boolean.TRUE.equals(member.getIsCreator()) ? "INITIATOR" : "MEMBER");
            vo.setMyJoinStatus(member.getJoinStatus());
            vo.setMyPayStatus(member.getPayStatus());
            vo.setMyReceiveStatus(member.getReceiveStatus());
            vo.setRefundAmountTotal(member.getRefundAmountTotal());
            vo.setCreatedAt(order.getCreatedAt());
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return new PageVO<>(list, memberPage.getTotal(), memberPage.getCurrent(), memberPage.getSize(), memberPage.getPages());
    }

    @Override
    @Transactional
    public void joinOrder(Long orderId, JoinOrderRequest request, Long userId) {
        GroupOrder order = requireOrder(orderId);
        if (!"OPEN".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (order.getCurrentMemberCount() >= order.getTotalMemberCount()) {
            throw new BusinessException(ErrorCode.ORDER_FULL);
        }

        GroupOrderMember existingMember = groupOrderMemberMapper.selectOne(
                new LambdaQueryWrapper<GroupOrderMember>()
                        .eq(GroupOrderMember::getGroupOrderId, orderId)
                        .eq(GroupOrderMember::getUserId, userId)
        );
        if (existingMember != null) {
            if ("ACTIVE".equals(existingMember.getJoinStatus())) {
                throw new BusinessException(ErrorCode.ORDER_ALREADY_JOINED);
            }
            if (!List.of("EXITED", "REFUNDED").contains(existingMember.getJoinStatus())) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "当前成员状态不允许重新加入");
            }

            existingMember.setRemark(request == null ? null : request.getRemark());
            existingMember.setJoinStatus("ACTIVE");
            existingMember.setPayStatus("UNPAID");
            existingMember.setPayAmount(order.getEstimatedPerAmount());
            existingMember.setPaidAt(null);
            existingMember.setRefundAmountTotal(BigDecimal.ZERO);
            existingMember.setReceiveStatus("NOT_READY");
            existingMember.setReceivedAt(null);
            groupOrderMemberMapper.updateById(existingMember);

            order.setCurrentMemberCount(order.getCurrentMemberCount() + 1);
            groupOrderMapper.updateById(order);
            insertOperationLog("USER", userId, "ORDER", orderId, "MEMBER_REJOINED",
                    request == null ? null : request.getRemark());
            return;
        }

        GroupOrderMember member = new GroupOrderMember();
        member.setGroupOrderId(orderId);
        member.setUserId(userId);
        member.setIsCreator(false);
        member.setRole("MEMBER");
        member.setRemark(request == null ? null : request.getRemark());
        member.setJoinStatus("ACTIVE");
        member.setPayStatus("UNPAID");
        member.setPayAmount(order.getEstimatedPerAmount());
        member.setRefundAmountTotal(BigDecimal.ZERO);
        member.setReceiveStatus("NOT_READY");
        groupOrderMemberMapper.insert(member);

        order.setCurrentMemberCount(order.getCurrentMemberCount() + 1);
        groupOrderMapper.updateById(order);
        insertOperationLog("USER", userId, "ORDER", orderId, "MEMBER_JOINED", request == null ? null : request.getRemark());
    }

    @Override
    @Transactional
    public void payOrder(Long orderId, Long userId) {
        GroupOrder order = requireOrder(orderId);
        if (!"OPEN".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        GroupOrderMember member = requireMember(orderId, userId);
        if (!"ACTIVE".equals(member.getJoinStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "当前成员状态不允许支付");
        }
        if (!"UNPAID".equals(member.getPayStatus())) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID);
        }

        member.setPayStatus("PAID");
        member.setPaidAt(LocalDateTime.now());
        groupOrderMemberMapper.updateById(member);
        insertCapitalRecord(generateCapitalBizNo("PAY"), userId, orderId, member.getId(),
                "PAY", member.getPayAmount(), "成员支付拼单预估金额", "USER", userId);
        insertOperationLog("USER", userId, "ORDER", orderId, "MEMBER_PAID", null);
        tryGroupOrder(order);
    }

    @Override
    @Transactional
    public void exitOrder(Long orderId, Long userId) {
        GroupOrder order = requireOrder(orderId);
        GroupOrderMember member = requireMember(orderId, userId);

        if (!"OPEN".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "只有开放中的订单才能退出");
        }
        if (Boolean.TRUE.equals(member.getIsCreator())) {
            throw new BusinessException(ErrorCode.INITIATOR_EXIT_NOT_ALLOWED);
        }
        if (!"ACTIVE".equals(member.getJoinStatus()) || !List.of("UNPAID", "PAID").contains(member.getPayStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "当前成员状态不允许退出");
        }

        if ("PAID".equals(member.getPayStatus())) {
            BigDecimal paidAmount = member.getPayAmount() == null ? BigDecimal.ZERO : member.getPayAmount();
            BigDecimal refunded = member.getRefundAmountTotal() == null ? BigDecimal.ZERO : member.getRefundAmountTotal();
            BigDecimal netRefund = paidAmount.subtract(refunded).max(BigDecimal.ZERO);
            member.setRefundAmountTotal(refunded.add(netRefund));
            member.setPayStatus("REFUNDED");
            member.setJoinStatus("REFUNDED");
            insertCapitalRecord(generateCapitalBizNo("RFE"), userId, orderId, member.getId(),
                    "REFUND_EXIT", netRefund, "成员退出拼单全额退款", "USER", userId);
        } else {
            member.setJoinStatus("EXITED");
        }
        groupOrderMemberMapper.updateById(member);
        order.setCurrentMemberCount(Math.max(order.getCurrentMemberCount() - 1, 1));
        groupOrderMapper.updateById(order);
        insertOperationLog("USER", userId, "ORDER", orderId, "MEMBER_EXITED", null);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        GroupOrder order = requireOrder(orderId);
        if (!"OPEN".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "只有拼单中的订单才能取消");
        }
        if (!Objects.equals(order.getCreatorUserId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有发起人可以取消拼单");
        }

        List<GroupOrderMember> activeMembers = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>()
                        .eq(GroupOrderMember::getGroupOrderId, orderId)
                        .eq(GroupOrderMember::getJoinStatus, "ACTIVE")
        );
        boolean hasParticipant = activeMembers.stream()
                .anyMatch(member -> !Boolean.TRUE.equals(member.getIsCreator()));
        if (hasParticipant) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "已有参与者加入，不能取消拼单");
        }

        cancelActiveMembers(orderId, "USER", userId, "发起人取消拼单全额退款");
        order.setStatus("CANCELED");
        order.setCancelReason("CREATOR_CANCELED");
        groupOrderMapper.updateById(order);
        insertOperationLog("USER", userId, "ORDER", orderId, "ORDER_CANCELED_BY_CREATOR", null);
        notifyOrderMembers(orderId, "ORDER_CANCELED", "拼单已取消",
                "发起人已取消拼单，已支付金额已原路退款。", true);
    }

    @Override
    @Transactional
    public void uploadReceipt(Long orderId, UploadReceiptRequest request, MultipartFile image, Long userId) {
        GroupOrder order = requireOrder(orderId);
        if (!"GROUPED".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "只有已成团订单才能上传凭证");
        }
        if (!Objects.equals(order.getCreatorUserId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有发起人可以上传凭证");
        }
        if (order.getReceiptUploadDeadlineAt() == null
                || LocalDateTime.now().isAfter(order.getReceiptUploadDeadlineAt())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "成团超过30分钟，不能上传凭证");
        }
        if (!request.getExpectedDeliveryEndAt().isAfter(request.getExpectedDeliveryStartAt())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "预计最晚送达时间必须晚于开始送达时间");
        }
        if (!request.getExpectedDeliveryStartAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "预计开始送达时间必须晚于当前时间");
        }
        if (request.getActualTotalAmount().compareTo(order.getEstimatedTotalAmount()) > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "实际总金额不能高于预计总金额");
        }
        Long receiptCount = orderReceiptMapper.selectCount(
                new LambdaQueryWrapper<OrderReceipt>().eq(OrderReceipt::getGroupOrderId, orderId)
        );
        if (receiptCount != null && receiptCount > 0) {
            throw new BusinessException(ErrorCode.RECEIPT_ALREADY_UPLOADED);
        }
        String imageUrl = receiptStorageService.store(image);

        OrderReceipt receipt = new OrderReceipt();
        receipt.setGroupOrderId(orderId);
        receipt.setUploaderUserId(userId);
        receipt.setImageUrl(imageUrl);
        receipt.setActualTotalAmount(request.getActualTotalAmount());
        receipt.setExpectedDeliveryStartAt(request.getExpectedDeliveryStartAt());
        receipt.setExpectedDeliveryEndAt(request.getExpectedDeliveryEndAt());
        receipt.setUploadedAt(LocalDateTime.now());
        orderReceiptMapper.insert(receipt);

        order.setActualTotalAmount(request.getActualTotalAmount());
        order.setActualPerAmount(request.getActualTotalAmount().divide(
                BigDecimal.valueOf(order.getTotalMemberCount()),
                2,
                RoundingMode.HALF_UP
        ));
        order.setExpectedDeliveryStartAt(request.getExpectedDeliveryStartAt());
        order.setExpectedDeliveryEndAt(request.getExpectedDeliveryEndAt());
        order.setReceiptUploadDeadlineAt(null);
        order.setStatus("WAIT_DELIVERY");
        groupOrderMapper.updateById(order);
        refundActualAmountDifference(order, request.getActualTotalAmount());
        insertOperationLog("USER", userId, "ORDER", orderId, "RECEIPT_UPLOADED", imageUrl);
    }

    @Override
    @Transactional
    public void markDelivered(Long orderId, Long userId) {
        GroupOrder order = requireOrder(orderId);
        if (!"WAIT_DELIVERY".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "只有待送达订单才能确认送达");
        }
        if (!Objects.equals(order.getCreatorUserId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有发起人可以确认送达");
        }

        LocalDateTime deliveredAt = LocalDateTime.now();
        order.setStatus("WAIT_RECEIVE");
        order.setDeliveredAt(deliveredAt);
        groupOrderMapper.updateById(order);

        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>()
                        .eq(GroupOrderMember::getGroupOrderId, orderId)
                        .eq(GroupOrderMember::getJoinStatus, "ACTIVE")
        );
        for (GroupOrderMember member : members) {
            if (Boolean.TRUE.equals(member.getIsCreator())) {
                member.setReceiveStatus("RECEIVED");
                member.setReceivedAt(deliveredAt);
            } else {
                member.setReceiveStatus("WAIT_CONFIRM");
                member.setReceivedAt(null);
            }
            groupOrderMemberMapper.updateById(member);
        }
        insertOperationLog("USER", userId, "ORDER", orderId, "ORDER_DELIVERED", null);
        notifyOrderMembers(orderId, "ORDER_DELIVERED", "订单已送达",
                "发起人已确认送达，请及时确认收货。", false);
    }

    @Override
    @Transactional
    public void confirmReceived(Long orderId, Long userId) {
        GroupOrder order = requireOrder(orderId);
        if (!"WAIT_RECEIVE".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "只有待收货订单才能确认收货");
        }

        GroupOrderMember member = requireMember(orderId, userId);
        if (!"ACTIVE".equals(member.getJoinStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "当前成员状态不允许确认收货");
        }
        if (Boolean.TRUE.equals(member.getIsCreator())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "发起人无需重复确认收货");
        }
        if (!"WAIT_CONFIRM".equals(member.getReceiveStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "当前收货状态不允许确认");
        }

        member.setReceiveStatus("RECEIVED");
        member.setReceivedAt(LocalDateTime.now());
        groupOrderMemberMapper.updateById(member);
        insertOperationLog("USER", userId, "ORDER", orderId, "MEMBER_RECEIVED", null);
        tryCompleteOrder(orderId);
    }

    @Override
    @Transactional
    public void processAutoGroup() {
        recoverCompletedOrders();
    }

    @Override
    @Transactional
    public void processTimeoutCancel() {
        cancelExpiredOpenOrders();
    }

    @Override
    @Transactional
    public void cancelExpiredOpenOrders() {
        List<GroupOrder> expiredOrders = groupOrderMapper.selectList(
                new LambdaQueryWrapper<GroupOrder>()
                        .eq(GroupOrder::getStatus, "OPEN")
                        .le(GroupOrder::getDeadlineAt, LocalDateTime.now())
        );

        for (GroupOrder order : expiredOrders) {
            order.setStatus("CANCELED");
            order.setCancelReason("DEADLINE_NOT_GROUPED");
            groupOrderMapper.updateById(order);
            cancelActiveMembers(order.getId(), "SYSTEM", null, "订单超时取消全额退款");
            insertOperationLog("SYSTEM", null, "ORDER", order.getId(), "ORDER_AUTO_CANCELED", "DEADLINE_NOT_GROUPED");
            notifyOrderMembers(order.getId(), "ORDER_CANCELED", "拼单已自动取消",
                    "订单超时未成团，系统已取消订单并处理退款。", true);
        }
    }

    @Override
    @Transactional
    public void openReceiptTimeoutComplaints() {
        List<GroupOrder> timeoutOrders = groupOrderMapper.selectList(
                new LambdaQueryWrapper<GroupOrder>()
                        .eq(GroupOrder::getStatus, "GROUPED")
                        .eq(GroupOrder::getComplaintOpened, false)
                        .le(GroupOrder::getReceiptUploadDeadlineAt, LocalDateTime.now())
        );

        for (GroupOrder order : timeoutOrders) {
            Long receiptCount = orderReceiptMapper.selectCount(
                    new LambdaQueryWrapper<OrderReceipt>().eq(OrderReceipt::getGroupOrderId, order.getId())
            );
            if (receiptCount == 0) {
                order.setComplaintOpened(true);
                groupOrderMapper.updateById(order);
                insertOperationLog("SYSTEM", null, "ORDER", order.getId(), "COMPLAINT_CHANNEL_OPENED",
                        "RECEIPT_TIMEOUT");
                notifyActiveMembers(order.getId(), "COMPLAINT_CHANNEL_OPENED", "投诉通道已开启",
                        "发起人超时未上传凭证，系统已为该订单开启投诉通道。", false);
            }
        }
    }

    @Override
    @Transactional
    public void openDeliveryTimeoutComplaints() {
        List<GroupOrder> timeoutOrders = groupOrderMapper.selectList(
                new LambdaQueryWrapper<GroupOrder>()
                        .eq(GroupOrder::getStatus, "WAIT_DELIVERY")
                        .eq(GroupOrder::getComplaintOpened, false)
                        .isNotNull(GroupOrder::getExpectedDeliveryEndAt)
                        .isNull(GroupOrder::getDeliveredAt)
                        .le(GroupOrder::getExpectedDeliveryEndAt, LocalDateTime.now().minusMinutes(60))
        );

        for (GroupOrder order : timeoutOrders) {
            order.setComplaintOpened(true);
            groupOrderMapper.updateById(order);
            insertOperationLog("SYSTEM", null, "ORDER", order.getId(), "COMPLAINT_CHANNEL_OPENED",
                    "DELIVERY_TIMEOUT");
            notifyActiveMembers(order.getId(), "COMPLAINT_CHANNEL_OPENED", "投诉通道已开启",
                    "订单超过预计最晚送达时间，系统已开启投诉通道。", false);
        }
    }

    @Override
    @Transactional
    public void autoConfirmReceivedMembers() {
        List<GroupOrder> receiveOrders = groupOrderMapper.selectList(
                new LambdaQueryWrapper<GroupOrder>()
                        .eq(GroupOrder::getStatus, "WAIT_RECEIVE")
                        .isNotNull(GroupOrder::getDeliveredAt)
                        .le(GroupOrder::getDeliveredAt, LocalDateTime.now().minusMinutes(30))
        );

        for (GroupOrder order : receiveOrders) {
            List<GroupOrderMember> waitMembers = groupOrderMemberMapper.selectList(
                    new LambdaQueryWrapper<GroupOrderMember>()
                            .eq(GroupOrderMember::getGroupOrderId, order.getId())
                            .eq(GroupOrderMember::getJoinStatus, "ACTIVE")
                            .eq(GroupOrderMember::getReceiveStatus, "WAIT_CONFIRM")
            );

            for (GroupOrderMember member : waitMembers) {
                member.setReceiveStatus("AUTO_RECEIVED");
                member.setReceivedAt(LocalDateTime.now());
                groupOrderMemberMapper.updateById(member);
                insertOperationLog("SYSTEM", null, "ORDER", order.getId(), "MEMBER_AUTO_RECEIVED",
                        "memberId=" + member.getId());
                insertNotification(order.getCreatorUserId(), "MEMBER_AUTO_RECEIVED", "成员已自动确认收货",
                        "订单中有成员超时未确认，系统已自动确认收货。", order.getId(), null);
            }

            tryCompleteOrder(order.getId());
        }
    }

    @Override
    @Transactional
    public void recoverCompletedOrders() {
        List<GroupOrder> receiveOrders = groupOrderMapper.selectList(
                new LambdaQueryWrapper<GroupOrder>().eq(GroupOrder::getStatus, "WAIT_RECEIVE")
        );
        receiveOrders.forEach(order -> tryCompleteOrder(order.getId()));
    }

    private void tryGroupOrder(GroupOrder order) {
        if (!"OPEN".equals(order.getStatus())) {
            return;
        }

        List<GroupOrderMember> activeMembers = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>()
                        .eq(GroupOrderMember::getGroupOrderId, order.getId())
                        .eq(GroupOrderMember::getJoinStatus, "ACTIVE")
        );
        boolean memberCountMatched = activeMembers.size() == order.getTotalMemberCount();
        boolean allPaid = activeMembers.stream().allMatch(item -> "PAID".equals(item.getPayStatus()));
        if (memberCountMatched && allPaid) {
            order.setCurrentMemberCount(activeMembers.size());
            order.setStatus("GROUPED");
            order.setReceiptUploadDeadlineAt(LocalDateTime.now().plusMinutes(30));
            groupOrderMapper.updateById(order);
            insertOperationLog("SYSTEM", null, "ORDER", order.getId(), "ORDER_GROUPED", null);
            notifyActiveMembers(order.getId(), "ORDER_GROUPED", "拼单已成团",
                    "订单已满员且成员均已支付，请关注后续凭证和送达进度。", true);
        }
    }

    private void tryCompleteOrder(Long orderId) {
        GroupOrder order = groupOrderMapper.selectById(orderId);
        if (order == null || !"WAIT_RECEIVE".equals(order.getStatus())) {
            return;
        }

        List<GroupOrderMember> activeMembers = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>()
                        .eq(GroupOrderMember::getGroupOrderId, orderId)
                        .eq(GroupOrderMember::getJoinStatus, "ACTIVE")
        );
        boolean allReceived = !activeMembers.isEmpty() && activeMembers.stream().allMatch(item ->
                "RECEIVED".equals(item.getReceiveStatus()) || "AUTO_RECEIVED".equals(item.getReceiveStatus()));
        if (allReceived) {
            order.setStatus("COMPLETED");
            groupOrderMapper.updateById(order);
            BigDecimal settleAmount = order.getActualTotalAmount() == null
                    ? activeMembers.stream()
                    .filter(member -> "PAID".equals(member.getPayStatus()))
                    .map(member -> member.getPayAmount() == null ? BigDecimal.ZERO : member.getPayAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    : order.getActualTotalAmount();
            insertCapitalRecord("SET-O" + orderId, order.getCreatorUserId(), orderId, null,
                    "SETTLE_TO_CREATOR", settleAmount, "订单完成后按实际总金额结算给发起人",
                    "SYSTEM", null);
            applyOrderFinishedCreditRewards(activeMembers, orderId);
            insertOperationLog("SYSTEM", null, "ORDER", orderId, "ORDER_COMPLETED", null);
            notifyActiveMembers(orderId, "ORDER_COMPLETED", "订单已完成",
                    "全部成员已确认收货，订单已完成并生成结算记录。", true);
        }
    }

    private void cancelActiveMembers(Long orderId, String operatorType, Long operatorId, String remark) {
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>().eq(GroupOrderMember::getGroupOrderId, orderId)
        );
        for (GroupOrderMember member : members) {
            if ("ACTIVE".equals(member.getJoinStatus()) && "PAID".equals(member.getPayStatus())) {
                BigDecimal paidAmount = member.getPayAmount() == null ? BigDecimal.ZERO : member.getPayAmount();
                BigDecimal refunded = member.getRefundAmountTotal() == null ? BigDecimal.ZERO : member.getRefundAmountTotal();
                BigDecimal netRefund = paidAmount.subtract(refunded).max(BigDecimal.ZERO);
                member.setRefundAmountTotal(refunded.add(netRefund));
                member.setPayStatus("REFUNDED");
                insertCapitalRecord("RFC-M" + member.getId(), member.getUserId(), orderId,
                        member.getId(), "REFUND_CANCEL", netRefund, remark, operatorType, operatorId);
            }
            if ("ACTIVE".equals(member.getJoinStatus())) {
                member.setJoinStatus("CANCELED");
            }
            groupOrderMemberMapper.updateById(member);
        }
    }

    private void refundActualAmountDifference(GroupOrder order, BigDecimal actualTotalAmount) {
        if (order.getEstimatedTotalAmount() == null || actualTotalAmount == null) {
            return;
        }
        BigDecimal diffTotal = order.getEstimatedTotalAmount().subtract(actualTotalAmount);
        if (diffTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal diffPerMember = diffTotal.divide(
                BigDecimal.valueOf(order.getTotalMemberCount()),
                2,
                RoundingMode.HALF_UP
        );
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>()
                        .eq(GroupOrderMember::getGroupOrderId, order.getId())
                        .eq(GroupOrderMember::getJoinStatus, "ACTIVE")
                        .eq(GroupOrderMember::getPayStatus, "PAID")
        );
        for (GroupOrderMember member : members) {
            BigDecimal refunded = member.getRefundAmountTotal() == null ? BigDecimal.ZERO : member.getRefundAmountTotal();
            BigDecimal payableDiff = member.getPayAmount() == null
                    ? diffPerMember
                    : member.getPayAmount().subtract(refunded).min(diffPerMember).max(BigDecimal.ZERO);
            if (payableDiff.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            member.setRefundAmountTotal(refunded.add(payableDiff));
            groupOrderMemberMapper.updateById(member);
            insertCapitalRecord("RFD-M" + member.getId(), member.getUserId(),
                    order.getId(), member.getId(), "REFUND_DIFF", payableDiff,
                    "实际金额低于预估金额差额退款", "SYSTEM", null);
        }
    }

    private void insertCapitalRecord(String bizNo, Long userId, Long orderId, Long memberId,
                                     String type, BigDecimal amount, String remark,
                                     String operatorType, Long operatorId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        Long existingCount = capitalRecordMapper.selectCount(
                new LambdaQueryWrapper<CapitalRecord>().eq(CapitalRecord::getBizNo, bizNo)
        );
        if (existingCount != null && existingCount > 0) {
            return;
        }

        CapitalRecord record = new CapitalRecord();
        record.setBizNo(bizNo);
        record.setUserId(userId);
        record.setGroupOrderId(orderId);
        record.setMemberId(memberId);
        record.setType(type);
        record.setAmount(amount);
        record.setStatus("SUCCESS");
        record.setRemark(remark);
        record.setOperatorType(operatorType);
        record.setOperatorId(operatorId);
        record.setCreatedAt(LocalDateTime.now());
        capitalRecordMapper.insert(record);
    }

    private String generateCapitalBizNo(String prefix) {
        return prefix + "-" + IdUtil.getSnowflakeNextIdStr();
    }

    private void applyOrderFinishedCreditRewards(List<GroupOrderMember> activeMembers, Long orderId) {
        for (GroupOrderMember member : activeMembers) {
            if (member.getUserId() == null) {
                continue;
            }

            Long existingCount = creditChangeRecordMapper.selectCount(
                    new LambdaQueryWrapper<CreditChangeRecord>()
                            .eq(CreditChangeRecord::getUserId, member.getUserId())
                            .eq(CreditChangeRecord::getRelatedOrderId, orderId)
                            .eq(CreditChangeRecord::getReasonType, "ORDER_FINISHED")
            );
            if (existingCount != null && existingCount > 0) {
                continue;
            }

            UserAccount user = userAccountMapper.selectById(member.getUserId());
            if (user == null) {
                continue;
            }

            int delta = 2;
            int currentScore = user.getCreditScore() == null ? 0 : user.getCreditScore();
            user.setCreditScore(Math.min(100, currentScore + delta));
            userAccountMapper.updateById(user);

            CreditChangeRecord record = new CreditChangeRecord();
            record.setUserId(user.getId());
            record.setChangeValue(delta);
            record.setReasonType("ORDER_FINISHED");
            record.setRelatedOrderId(orderId);
            record.setRemark("完成有效拼单奖励信用分");
            record.setCreatedAt(LocalDateTime.now());
            creditChangeRecordMapper.insert(record);
        }
    }

    private void insertOperationLog(String operatorType, Long operatorId, String bizType, Long bizId,
                                    String action, String detail) {
        OperationLog log = new OperationLog();
        log.setOperatorType(operatorType);
        log.setOperatorId(operatorId);
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setAction(action);
        log.setDetailJson(JSONUtil.createObj()
                .set("detail", detail == null ? "" : detail)
                .toString());
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private void notifyActiveMembers(Long orderId, String type, String title, String content, boolean includeCreator) {
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>()
                        .eq(GroupOrderMember::getGroupOrderId, orderId)
                        .eq(GroupOrderMember::getJoinStatus, "ACTIVE")
        );
        for (GroupOrderMember member : members) {
            if (!includeCreator && Boolean.TRUE.equals(member.getIsCreator())) {
                continue;
            }
            insertNotification(member.getUserId(), type, title, content, orderId, null);
        }
    }

    private void notifyOrderMembers(Long orderId, String type, String title, String content, boolean includeCreator) {
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>().eq(GroupOrderMember::getGroupOrderId, orderId)
        );
        for (GroupOrderMember member : members) {
            if (!includeCreator && Boolean.TRUE.equals(member.getIsCreator())) {
                continue;
            }
            insertNotification(member.getUserId(), type, title, content, orderId, null);
        }
    }

    private void insertNotification(Long userId, String type, String title, String content,
                                    Long orderId, Long complaintId) {
        if (userId == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);
        notification.setRelatedOrderId(orderId);
        notification.setRelatedComplaintId(complaintId);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    private OrderBasicInfoVO buildBasicInfo(GroupOrder order) {
        OrderBasicInfoVO basicInfo = new OrderBasicInfoVO();
        basicInfo.setOrderId(order.getId());
        basicInfo.setOrderNo(order.getOrderNo());
        basicInfo.setProductName(order.getProductName());
        basicInfo.setProductDesc(order.getProductDesc());
        basicInfo.setTotalMemberCount(order.getTotalMemberCount());
        basicInfo.setCurrentMemberCount(order.getCurrentMemberCount());
        basicInfo.setEstimatedTotalAmount(order.getEstimatedTotalAmount());
        basicInfo.setEstimatedPerAmount(order.getEstimatedPerAmount());
        basicInfo.setActualTotalAmount(order.getActualTotalAmount());
        basicInfo.setActualPerAmount(order.getActualPerAmount());
        basicInfo.setPickupPoint(order.getPickupPoint());
        basicInfo.setStatus(order.getStatus());
        basicInfo.setDeadlineAt(order.getDeadlineAt());
        basicInfo.setReceiptUploadDeadlineAt(order.getReceiptUploadDeadlineAt());
        basicInfo.setExpectedDeliveryStartAt(order.getExpectedDeliveryStartAt());
        basicInfo.setExpectedDeliveryEndAt(order.getExpectedDeliveryEndAt());
        basicInfo.setDeliveredAt(order.getDeliveredAt());
        basicInfo.setComplaintOpened(order.getComplaintOpened());
        basicInfo.setCreatedAt(order.getCreatedAt());
        return basicInfo;
    }

    private OrderInitiatorInfoVO buildInitiatorInfo(UserAccount creator) {
        OrderInitiatorInfoVO initiatorInfo = new OrderInitiatorInfoVO();
        if (creator == null) {
            return initiatorInfo;
        }
        initiatorInfo.setUserId(creator.getId());
        initiatorInfo.setNickname(creator.getNickname());
        initiatorInfo.setPhone(creator.getPhone());
        return initiatorInfo;
    }

    private OrderMemberVO buildMemberVO(GroupOrderMember member) {
        OrderMemberVO memberVO = new OrderMemberVO();
        UserAccount user = userAccountMapper.selectById(member.getUserId());
        memberVO.setUserId(member.getUserId());
        memberVO.setNickname(user == null ? "" : user.getNickname());
        memberVO.setIsCreator(member.getIsCreator());
        memberVO.setRole(member.getRole());
        memberVO.setRemark(member.getRemark());
        memberVO.setJoinStatus(member.getJoinStatus());
        memberVO.setPayStatus(member.getPayStatus());
        memberVO.setPayAmount(member.getPayAmount());
        memberVO.setPaidAt(member.getPaidAt());
        memberVO.setRefundAmountTotal(member.getRefundAmountTotal());
        memberVO.setReceiveStatus(member.getReceiveStatus());
        memberVO.setReceivedAt(member.getReceivedAt());
        memberVO.setJoinedAt(member.getCreatedAt());
        return memberVO;
    }

    private PaymentSummaryVO buildPaymentSummary(GroupOrder order, List<GroupOrderMember> members) {
        PaymentSummaryVO paymentSummary = new PaymentSummaryVO();
        paymentSummary.setEstimatedTotalAmount(order.getEstimatedTotalAmount());
        paymentSummary.setActualTotalAmount(order.getActualTotalAmount());
        paymentSummary.setTotalMembers((int) members.stream().filter(item -> "ACTIVE".equals(item.getJoinStatus())).count());
        paymentSummary.setPaidMembers((int) members.stream()
                .filter(item -> "ACTIVE".equals(item.getJoinStatus()) && "PAID".equals(item.getPayStatus()))
                .count());
        paymentSummary.setTotalPaidAmount(members.stream()
                .filter(item -> "ACTIVE".equals(item.getJoinStatus()) && "PAID".equals(item.getPayStatus()))
                .map(GroupOrderMember::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        paymentSummary.setRefundTotalAmount(members.stream()
                .map(GroupOrderMember::getRefundAmountTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return paymentSummary;
    }

    private ReceiptInfoVO buildReceiptInfo(OrderReceipt receipt) {
        if (receipt == null) {
            return null;
        }
        ReceiptInfoVO receiptInfoVO = new ReceiptInfoVO();
        receiptInfoVO.setReceiptId(receipt.getId());
        receiptInfoVO.setImageUrl(receipt.getImageUrl());
        receiptInfoVO.setActualTotalAmount(receipt.getActualTotalAmount());
        receiptInfoVO.setExpectedDeliveryStartAt(receipt.getExpectedDeliveryStartAt());
        receiptInfoVO.setExpectedDeliveryEndAt(receipt.getExpectedDeliveryEndAt());
        receiptInfoVO.setUploadedAt(receipt.getUploadedAt());
        UserAccount uploader = userAccountMapper.selectById(receipt.getUploaderUserId());
        receiptInfoVO.setUploaderNickname(uploader == null ? "" : uploader.getNickname());
        return receiptInfoVO;
    }

    private ReceiveInfoVO buildReceiveInfo(GroupOrder order, List<OrderMemberVO> members) {
        ReceiveInfoVO receiveInfo = new ReceiveInfoVO();
        receiveInfo.setDeliveredAt(order.getDeliveredAt());
        receiveInfo.setAutoConfirmDeadlineAt(order.getDeliveredAt() == null ? null : order.getDeliveredAt().plusMinutes(30));
        List<OrderMemberVO> activeMembers = members.stream()
                .filter(item -> "ACTIVE".equals(item.getJoinStatus()))
                .collect(Collectors.toList());
        receiveInfo.setTotalMembers(activeMembers.size());
        receiveInfo.setReceivedMembers((int) activeMembers.stream()
                .filter(item -> "RECEIVED".equals(item.getReceiveStatus()) || "AUTO_RECEIVED".equals(item.getReceiveStatus()))
                .count());
        receiveInfo.setReceiveStatusSummary(activeMembers);
        return receiveInfo;
    }

    private ComplaintInfoVO buildComplaintInfo(GroupOrder order, List<Complaint> complaints, Long userId) {
        ComplaintInfoVO complaintInfo = new ComplaintInfoVO();
        complaintInfo.setComplaintOpened(order.getComplaintOpened());
        complaintInfo.setComplaintCount(complaints.size());
        complaints.stream()
                .filter(item -> Objects.equals(item.getComplainantUserId(), userId))
                .findFirst()
                .ifPresent(item -> {
                    complaintInfo.setComplaintId(item.getId());
                    complaintInfo.setComplaintNo(item.getComplaintNo());
                    complaintInfo.setType(item.getType());
                    complaintInfo.setStatus(item.getStatus());
                    complaintInfo.setMyComplaintId(item.getId());
                    complaintInfo.setMyComplaintStatus(item.getStatus());
                    complaintInfo.setCreated_at(item.getCreatedAt());
                });
        return complaintInfo;
    }

    private ActionFlagsVO buildActionFlags(GroupOrder order, GroupOrderMember currentMember, OrderReceipt receipt, List<Complaint> complaints, Long userId) {
        ActionFlagsVO actionFlags = new ActionFlagsVO();
        boolean hasMyComplaint = complaints.stream().anyMatch(item -> Objects.equals(item.getComplainantUserId(), userId));
        actionFlags.setCanJoin("OPEN".equals(order.getStatus())
                && (currentMember == null || List.of("EXITED", "REFUNDED").contains(currentMember.getJoinStatus()))
                && order.getCurrentMemberCount() < order.getTotalMemberCount());
        actionFlags.setCanPay(currentMember != null
                && "OPEN".equals(order.getStatus())
                && "ACTIVE".equals(currentMember.getJoinStatus())
                && "UNPAID".equals(currentMember.getPayStatus()));
        actionFlags.setCanExit(currentMember != null
                && "OPEN".equals(order.getStatus())
                && "ACTIVE".equals(currentMember.getJoinStatus())
                && !Boolean.TRUE.equals(currentMember.getIsCreator())
                && List.of("UNPAID", "PAID").contains(currentMember.getPayStatus()));
        actionFlags.setCanCancel("OPEN".equals(order.getStatus())
                && Objects.equals(order.getCreatorUserId(), userId)
                && order.getCurrentMemberCount() != null
                && order.getCurrentMemberCount() == 1);
        actionFlags.setCanUploadReceipt("GROUPED".equals(order.getStatus())
                && Objects.equals(order.getCreatorUserId(), userId)
                && order.getReceiptUploadDeadlineAt() != null
                && !LocalDateTime.now().isAfter(order.getReceiptUploadDeadlineAt()));
        actionFlags.setCanMarkDelivered("WAIT_DELIVERY".equals(order.getStatus()) && Objects.equals(order.getCreatorUserId(), userId));
        actionFlags.setCanConfirmReceived(currentMember != null
                && "WAIT_RECEIVE".equals(order.getStatus())
                && "ACTIVE".equals(currentMember.getJoinStatus())
                && !Boolean.TRUE.equals(currentMember.getIsCreator())
                && "WAIT_CONFIRM".equals(currentMember.getReceiveStatus()));
        actionFlags.setCanCreateComplaint(Boolean.TRUE.equals(order.getComplaintOpened())
                && currentMember != null
                && "ACTIVE".equals(currentMember.getJoinStatus())
                && !Objects.equals(order.getCreatorUserId(), userId)
                && !hasMyComplaint);
        actionFlags.setCanViewReceipt(receipt != null);
        return actionFlags;
    }

    private List<TimelineItemVO> buildTimeline(GroupOrder order, UserAccount creator, OrderReceipt receipt, List<Complaint> complaints, Long userId) {
        List<TimelineItemVO> timeline = new ArrayList<>();
        timeline.add(buildTimelineItem("ORDER_CREATED", "创建拼单", order.getCreatedAt(), creator == null ? "系统" : creator.getNickname()));

        if (List.of("GROUPED", "WAIT_DELIVERY", "WAIT_RECEIVE", "COMPLETED").contains(order.getStatus())) {
            timeline.add(buildTimelineItem("ORDER_STATUS", "订单已成团", order.getUpdatedAt(), "系统"));
        }
        if (receipt != null) {
            timeline.add(buildTimelineItem("RECEIPT_UPLOADED", "已上传凭证", receipt.getUploadedAt(), creator == null ? "发起人" : creator.getNickname()));
        }
        if (order.getDeliveredAt() != null) {
            timeline.add(buildTimelineItem("ORDER_DELIVERED", "已确认送达", order.getDeliveredAt(), creator == null ? "发起人" : creator.getNickname()));
        }
        complaints.stream()
                .filter(item -> Objects.equals(item.getComplainantUserId(), userId))
                .findFirst()
                .ifPresent(item -> timeline.add(buildTimelineItem("COMPLAINT_CREATED", "当前账号已发起投诉", item.getCreatedAt(), "当前账号")));
        if ("COMPLETED".equals(order.getStatus())) {
            timeline.add(buildTimelineItem("ORDER_STATUS", "订单已完成", order.getUpdatedAt(), "系统"));
        }
        if ("CANCELED".equals(order.getStatus())) {
            timeline.add(buildTimelineItem("ORDER_STATUS", "订单已取消", order.getUpdatedAt(), "系统"));
        }
        return timeline;
    }

    private TimelineItemVO buildTimelineItem(String action, String description, LocalDateTime time, String operator) {
        TimelineItemVO item = new TimelineItemVO();
        item.setAction(action);
        item.setDescription(description);
        item.setTime(time);
        item.setOperator(operator);
        return item;
    }

    private GroupOrder requireOrder(Long orderId) {
        GroupOrder order = groupOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    private GroupOrderMember requireMember(Long orderId, Long userId) {
        GroupOrderMember member = groupOrderMemberMapper.selectOne(
                new LambdaQueryWrapper<GroupOrderMember>()
                        .eq(GroupOrderMember::getGroupOrderId, orderId)
                        .eq(GroupOrderMember::getUserId, userId)
        );
        if (member == null) {
            throw new BusinessException(ErrorCode.ORDER_MEMBER_REQUIRED);
        }
        return member;
    }

    private String generateOrderNo() {
        return "ORDER" + IdUtil.fastSimpleUUID().substring(0, 12).toUpperCase();
    }
}
