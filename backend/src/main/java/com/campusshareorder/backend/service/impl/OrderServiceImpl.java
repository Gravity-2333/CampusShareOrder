package com.campusshareorder.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusshareorder.backend.dto.order.CreateOrderRequest;
import com.campusshareorder.backend.dto.order.JoinOrderRequest;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.entity.GroupOrderMember;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.GroupOrderMapper;
import com.campusshareorder.backend.mapper.GroupOrderMemberMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.vo.order.*;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<GroupOrderMapper, GroupOrder> implements OrderService {

    private final GroupOrderMapper groupOrderMapper;
    private final GroupOrderMemberMapper groupOrderMemberMapper;
    private final UserAccountMapper userAccountMapper;

    @Override
    @Transactional
    public CreateOrderVO createOrder(CreateOrderRequest request, Long userId) {
        // 验证用户是否存在
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证输入数据
        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new RuntimeException("商品名称不能为空");
        }
        if (request.getTotalMemberCount() == null || request.getTotalMemberCount() < 2) {
            throw new RuntimeException("总人数至少为2人");
        }
        if (request.getEstimatedTotalAmount() == null || request.getEstimatedTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("预估总金额必须大于0");
        }
        if (request.getPickupPoint() == null || request.getPickupPoint().trim().isEmpty()) {
            throw new RuntimeException("取货点不能为空");
        }
        if (request.getDeadlineAt() == null || request.getDeadlineAt().trim().isEmpty()) {
            throw new RuntimeException("截止时间不能为空");
        }

        // 计算预估人均金额
        BigDecimal estimatedPerAmount = request.getEstimatedTotalAmount().divide(
                new BigDecimal(request.getTotalMemberCount()), 2, BigDecimal.ROUND_HALF_UP
        );

        // 创建订单
        GroupOrder order = new GroupOrder();
        order.setOrderNo(generateOrderNo());
        order.setCreatorUserId(userId);
        order.setProductName(request.getProductName());
        order.setProductDesc(request.getProductDesc());
        order.setTotalMemberCount(request.getTotalMemberCount());
        order.setCurrentMemberCount(1); // 发起人算一个
        order.setEstimatedTotalAmount(request.getEstimatedTotalAmount());
        order.setEstimatedPerAmount(estimatedPerAmount);
        order.setPickupPoint(request.getPickupPoint());
        order.setDeadlineAt(LocalDateTimeUtil.parse(request.getDeadlineAt(), "yyyy-MM-dd HH:mm:ss"));
        order.setStatus("拼单中");
        order.setComplaintOpened(false);
        groupOrderMapper.insert(order);

        // 创建发起人作为成员
        GroupOrderMember member = new GroupOrderMember();
        member.setGroupOrderId(order.getId());
        member.setUserId(userId);
        member.setIsCreator(true);
        member.setJoinStatus("待支付");
        member.setPayStatus("待支付");
        member.setReceiveStatus("待收货");
        groupOrderMemberMapper.insert(member);

        CreateOrderVO vo = new CreateOrderVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        return vo;
    }

    @Override
    public List<OrderListItemVO> getOrderList() {
        // 查询所有拼单中状态的订单
        LambdaQueryWrapper<GroupOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupOrder::getStatus, "拼单中")
                .orderByDesc(GroupOrder::getCreatedAt);
        List<GroupOrder> orders = groupOrderMapper.selectList(wrapper);

        return orders.stream().map(order -> {
            OrderListItemVO vo = new OrderListItemVO();
            vo.setOrderId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setProductName(order.getProductName());
            vo.setProductDesc(order.getProductDesc());
            vo.setTotalMemberCount(order.getTotalMemberCount());
            vo.setCurrentMemberCount(order.getCurrentMemberCount());
            vo.setEstimatedPerAmount(order.getEstimatedPerAmount());
            vo.setPickupPoint(order.getPickupPoint());
            vo.setStatus(order.getStatus());
            vo.setDeadlineAt(order.getDeadlineAt());
            vo.setCreatedAt(order.getCreatedAt());

            // 获取发起人信息
            UserAccount creator = userAccountMapper.selectById(order.getCreatorUserId());
            if (creator != null) {
                vo.setCreatorNickname(creator.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId, Long userId) {
        // 查询订单信息
        GroupOrder order = groupOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 查询成员信息
        LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupOrderMember::getGroupOrderId, orderId);
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(memberWrapper);

        // 构建详情VO
        OrderDetailVO detailVO = new OrderDetailVO();

        // 基础信息
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
        basicInfo.setCreatedAt(order.getCreatedAt());
        detailVO.setBasicInfo(basicInfo);

        // 发起人信息
        UserAccount creator = userAccountMapper.selectById(order.getCreatorUserId());
        if (creator != null) {
            OrderInitiatorInfoVO initiatorInfo = new OrderInitiatorInfoVO();
            initiatorInfo.setUserId(creator.getId());
            initiatorInfo.setNickname(creator.getNickname());
            initiatorInfo.setPhone(creator.getPhone());
            detailVO.setInitiatorInfo(initiatorInfo);
        }

        // 成员列表
        List<OrderMemberVO> memberVOList = members.stream().map(member -> {
            OrderMemberVO memberVO = new OrderMemberVO();
            UserAccount memberUser = userAccountMapper.selectById(member.getUserId());
            if (memberUser != null) {
                memberVO.setUserId(memberUser.getId());
                memberVO.setNickname(memberUser.getNickname());
            }
            memberVO.setIsCreator(member.getIsCreator());
            memberVO.setRemark(member.getRemark());
            memberVO.setJoinStatus(member.getJoinStatus());
            memberVO.setPayStatus(member.getPayStatus());
            memberVO.setPayAmount(member.getPayAmount());
            memberVO.setPaidAt(member.getPaidAt());
            memberVO.setRefundAmountTotal(member.getRefundAmountTotal());
            memberVO.setReceiveStatus(member.getReceiveStatus());
            memberVO.setReceivedAt(member.getReceivedAt());
            return memberVO;
        }).collect(Collectors.toList());
        detailVO.setMemberList(memberVOList);

        // 当前用户的成员信息
        GroupOrderMember currentMember = members.stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst().orElse(null);
        if (currentMember != null) {
            OrderMemberVO currentUserMember = new OrderMemberVO();
            UserAccount currentUser = userAccountMapper.selectById(userId);
            if (currentUser != null) {
                currentUserMember.setUserId(currentUser.getId());
                currentUserMember.setNickname(currentUser.getNickname());
            }
            currentUserMember.setIsCreator(currentMember.getIsCreator());
            currentUserMember.setRemark(currentMember.getRemark());
            currentUserMember.setJoinStatus(currentMember.getJoinStatus());
            currentUserMember.setPayStatus(currentMember.getPayStatus());
            currentUserMember.setPayAmount(currentMember.getPayAmount());
            currentUserMember.setPaidAt(currentMember.getPaidAt());
            currentUserMember.setRefundAmountTotal(currentMember.getRefundAmountTotal());
            currentUserMember.setReceiveStatus(currentMember.getReceiveStatus());
            currentUserMember.setReceivedAt(currentMember.getReceivedAt());
            detailVO.setCurrentUserMember(currentUserMember);
        }

        // 查看者角色
        if (order.getCreatorUserId().equals(userId)) {
            detailVO.setViewerRoleInOrder("CREATOR");
        } else if (currentMember != null) {
            detailVO.setViewerRoleInOrder("MEMBER");
        } else {
            detailVO.setViewerRoleInOrder("VIEWER");
        }

        // 支付汇总
        PaymentSummaryVO paymentSummary = new PaymentSummaryVO();
        paymentSummary.setTotalMembers(members.size());
        paymentSummary.setPaidMembers((int) members.stream()
                .filter(m -> "PAID".equals(m.getPayStatus()))
                .count());
        paymentSummary.setTotalPaidAmount(members.stream()
                .filter(m -> "PAID".equals(m.getPayStatus()))
                .map(GroupOrderMember::getPayAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        paymentSummary.setRefundTotalAmount(members.stream()
                .map(GroupOrderMember::getRefundAmountTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        detailVO.setPaymentSummary(paymentSummary);

        // 收货信息
        ReceiveInfoVO receiveInfo = new ReceiveInfoVO();
        receiveInfo.setDeliveredAt(order.getDeliveredAt());
        receiveInfo.setTotalMembers(members.size());
        receiveInfo.setReceivedMembers((int) members.stream()
                .filter(m -> "RECEIVED".equals(m.getReceiveStatus()) || "AUTO_RECEIVED".equals(m.getReceiveStatus()))
                .count());
        detailVO.setReceiveInfo(receiveInfo);

        // 投诉信息
        ComplaintInfoVO complaintInfo = new ComplaintInfoVO();
        complaintInfo.setComplaintOpened(order.getComplaintOpened());
        detailVO.setComplaintInfo(complaintInfo);

        // 操作标志
        ActionFlagsVO actionFlags = new ActionFlagsVO();
        actionFlags.setCanJoin("拼单中".equals(order.getStatus()) && currentMember == null);
        actionFlags.setCanExit("拼单中".equals(order.getStatus()) && currentMember != null && "待支付".equals(currentMember.getJoinStatus()));
        actionFlags.setCanPay(currentMember != null && "待支付".equals(currentMember.getPayStatus()));
        actionFlags.setCanUploadReceipt("已成团".equals(order.getStatus()) && order.getCreatorUserId().equals(userId));
        actionFlags.setCanMarkDelivered("待送达".equals(order.getStatus()) && order.getCreatorUserId().equals(userId));
        actionFlags.setCanConfirmReceived("待收货".equals(order.getStatus()) && currentMember != null && "待收货".equals(currentMember.getReceiveStatus()));
        actionFlags.setCanComplaint(order.getComplaintOpened() && currentMember != null && !order.getCreatorUserId().equals(userId));
        detailVO.setActionFlags(actionFlags);

        // 时间线
        List<TimelineItemVO> timeline = new ArrayList<>();
        TimelineItemVO createItem = new TimelineItemVO();
        createItem.setAction("ORDER_CREATE");
        createItem.setDescription("创建拼单");
        createItem.setTime(order.getCreatedAt());
        createItem.setOperator(creator != null ? creator.getNickname() : "系统");
        timeline.add(createItem);
        detailVO.setTimeline(timeline);

        return detailVO;
    }

    @Override
    public List<MyOrderListItemVO> getMyOrders(Long userId) {
        // 查询用户参与的所有订单
        LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupOrderMember::getUserId, userId);
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(memberWrapper);

        return members.stream().map(member -> {
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
            vo.setMyRole(member.getIsCreator() ? "CREATOR" : "MEMBER");
            vo.setMyJoinStatus(member.getJoinStatus());
            vo.setMyPayStatus(member.getPayStatus());
            vo.setMyReceiveStatus(member.getReceiveStatus());
            vo.setRefundAmountTotal(member.getRefundAmountTotal());
            vo.setCreatedAt(order.getCreatedAt());
            return vo;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void joinOrder(Long orderId, JoinOrderRequest request, Long userId) {
        // 验证订单是否存在
        GroupOrder order = groupOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态
        if (!"拼单中".equals(order.getStatus())) {
            throw new RuntimeException("订单已关闭，无法加入");
        }

        // 验证是否已满员
        if (order.getCurrentMemberCount() >= order.getTotalMemberCount()) {
            throw new RuntimeException("订单已满员");
        }

        // 验证用户是否已经加入
        LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupOrderMember::getGroupOrderId, orderId)
                .eq(GroupOrderMember::getUserId, userId);
        GroupOrderMember existingMember = groupOrderMemberMapper.selectOne(memberWrapper);
        if (existingMember != null) {
            throw new RuntimeException("您已经加入该订单");
        }

        // 创建成员记录
        GroupOrderMember member = new GroupOrderMember();
        member.setGroupOrderId(orderId);
        member.setUserId(userId);
        member.setIsCreator(false);
        member.setRemark(request.getRemark());
        member.setJoinStatus("待支付");
        member.setPayStatus("待支付");
        member.setReceiveStatus("待收货");
        groupOrderMemberMapper.insert(member);

        // 更新订单当前人数
        order.setCurrentMemberCount(order.getCurrentMemberCount() + 1);
        groupOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void payOrder(Long orderId, Long userId) {
        // 验证订单是否存在
        GroupOrder order = groupOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态
        if (!"拼单中".equals(order.getStatus())) {
            throw new RuntimeException("订单已关闭，无法支付");
        }

        // 查找用户在该订单中的成员记录
        LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupOrderMember::getGroupOrderId, orderId)
                .eq(GroupOrderMember::getUserId, userId);
        GroupOrderMember member = groupOrderMemberMapper.selectOne(memberWrapper);
        if (member == null) {
            throw new RuntimeException("您不是该订单的成员");
        }

        // 验证支付状态
        if (!"待支付".equals(member.getPayStatus())) {
            throw new RuntimeException("您已经支付过了");
        }

        // 更新支付状态
        member.setPayStatus("已支付");
        member.setJoinStatus("已支付");
        member.setPayAmount(order.getEstimatedPerAmount());
        member.setPaidAt(LocalDateTime.now());
        groupOrderMemberMapper.updateById(member);

        // 检查是否所有成员都已支付
        LambdaQueryWrapper<GroupOrderMember> allMemberWrapper = new LambdaQueryWrapper<>();
        allMemberWrapper.eq(GroupOrderMember::getGroupOrderId, orderId);
        List<GroupOrderMember> allMembers = groupOrderMemberMapper.selectList(allMemberWrapper);
        boolean allPaid = allMembers.stream().allMatch(m -> "已支付".equals(m.getPayStatus()));

        // 检查是否满员且全部支付
        if (allPaid && order.getCurrentMemberCount() >= order.getTotalMemberCount()) {
            // 自动成团
            order.setStatus("已成团");
            // 设置凭证上传截止时间（30分钟后）
            order.setReceiptUploadDeadlineAt(LocalDateTime.now().plusMinutes(30));
            groupOrderMapper.updateById(order);
        }
    }

    @Override
    @Transactional
    public void exitOrder(Long orderId, Long userId) {
        // 验证订单是否存在
        GroupOrder order = groupOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态
        if (!"拼单中".equals(order.getStatus())) {
            throw new RuntimeException("只有拼单中状态的订单可以退出");
        }

        // 查找用户在该订单中的成员记录
        LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupOrderMember::getGroupOrderId, orderId)
                .eq(GroupOrderMember::getUserId, userId);
        GroupOrderMember member = groupOrderMemberMapper.selectOne(memberWrapper);
        if (member == null) {
            throw new RuntimeException("您不是该订单的成员");
        }

        // 验证是否可以退出
        if (!"待支付".equals(member.getJoinStatus())) {
            throw new RuntimeException("您已经支付，无法退出");
        }

        // 删除成员记录
        groupOrderMemberMapper.deleteById(member.getId());

        // 更新订单当前人数
        order.setCurrentMemberCount(order.getCurrentMemberCount() - 1);
        groupOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void processAutoGroup() {
        // 查询所有拼单中状态的订单
        LambdaQueryWrapper<GroupOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(GroupOrder::getStatus, "拼单中");
        List<GroupOrder> orders = groupOrderMapper.selectList(orderWrapper);

        for (GroupOrder order : orders) {
            // 检查是否满员
            if (order.getCurrentMemberCount() >= order.getTotalMemberCount()) {
                // 检查是否所有成员都已支付
                LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
                memberWrapper.eq(GroupOrderMember::getGroupOrderId, order.getId());
                List<GroupOrderMember> members = groupOrderMemberMapper.selectList(memberWrapper);
                boolean allPaid = members.stream().allMatch(m -> "已支付".equals(m.getPayStatus()));

                if (allPaid) {
                    // 自动成团
                    order.setStatus("已成团");
                    order.setReceiptUploadDeadlineAt(LocalDateTime.now().plusMinutes(30));
                    groupOrderMapper.updateById(order);
                }
            }
        }
    }

    @Override
    @Transactional
    public void processTimeoutCancel() {
        // 查询所有拼单中状态且已过截止时间的订单
        LambdaQueryWrapper<GroupOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(GroupOrder::getStatus, "拼单中")
                .lt(GroupOrder::getDeadlineAt, LocalDateTime.now());
        List<GroupOrder> orders = groupOrderMapper.selectList(orderWrapper);

        for (GroupOrder order : orders) {
            // 取消订单
            order.setStatus("已取消");
            order.setCancelReason("未成团超时取消");
            groupOrderMapper.updateById(order);

            // 处理已支付成员的退款
            LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(GroupOrderMember::getGroupOrderId, order.getId())
                    .eq(GroupOrderMember::getPayStatus, "已支付");
            List<GroupOrderMember> paidMembers = groupOrderMemberMapper.selectList(memberWrapper);

            for (GroupOrderMember member : paidMembers) {
                // 更新成员状态为已退款
                member.setPayStatus("已退款");
                member.setJoinStatus("已退款");
                member.setRefundAmountTotal(member.getPayAmount());
                groupOrderMemberMapper.updateById(member);
            }
        }

        // 处理已成团但发起人未上传凭证的订单
        LambdaQueryWrapper<GroupOrder> receiptWrapper = new LambdaQueryWrapper<>();
        receiptWrapper.eq(GroupOrder::getStatus, "已成团")
                .lt(GroupOrder::getReceiptUploadDeadlineAt, LocalDateTime.now());
        List<GroupOrder> receiptOrders = groupOrderMapper.selectList(receiptWrapper);

        for (GroupOrder order : receiptOrders) {
            // 取消订单
            order.setStatus("已取消");
            order.setCancelReason("发起人未上传凭证");
            order.setComplaintOpened(true);
            groupOrderMapper.updateById(order);

            // 处理已支付成员的退款
            LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(GroupOrderMember::getGroupOrderId, order.getId())
                    .eq(GroupOrderMember::getPayStatus, "已支付");
            List<GroupOrderMember> paidMembers = groupOrderMemberMapper.selectList(memberWrapper);

            for (GroupOrderMember member : paidMembers) {
                // 更新成员状态为已退款
                member.setPayStatus("已退款");
                member.setJoinStatus("已退款");
                member.setRefundAmountTotal(member.getPayAmount());
                groupOrderMemberMapper.updateById(member);
            }
        }
    }

    private String generateOrderNo() {
        return "ORDER" + IdUtil.fastSimpleUUID().substring(0, 12).toUpperCase();
    }
}