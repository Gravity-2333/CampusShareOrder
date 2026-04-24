package com.campusshareorder.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.enums.OrderStatus;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.dto.admin.AdminCancelOrderRequest;
import com.campusshareorder.backend.dto.admin.AdminOrderQueryRequest;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.entity.GroupOrderMember;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.GroupOrderMapper;
import com.campusshareorder.backend.mapper.GroupOrderMemberMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.AdminOrderService;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.vo.order.OrderDetailVO;
import com.campusshareorder.backend.vo.order.OrderListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final GroupOrderMapper groupOrderMapper;
    private final GroupOrderMemberMapper groupOrderMemberMapper;
    private final UserAccountMapper userAccountMapper;
    private final OrderService orderService;

    @Override
    public PageResult<OrderListItemVO> listOrders(AdminOrderQueryRequest request) {
        Page<GroupOrder> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<GroupOrder> wrapper = new LambdaQueryWrapper<>();

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            wrapper.eq(GroupOrder::getStatus, request.getStatus());
        }
        if (request.getOrderNo() != null && !request.getOrderNo().trim().isEmpty()) {
            wrapper.like(GroupOrder::getOrderNo, request.getOrderNo());
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

        return new PageResult<>(list, request.getPage(), request.getPageSize(), orderPage.getTotal());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        Long currentUserId = null; // admin can view without joining
        return orderService.getOrderDetail(orderId, currentUserId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, AdminCancelOrderRequest request, Long adminId) {
        GroupOrder order = groupOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }

        if (order.getStatus().equals(OrderStatus.COMPLETED.getCode()) || order.getStatus().equals(OrderStatus.CANCELED.getCode())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "已完成或已取消订单不能取消");
        }

        order.setStatus(OrderStatus.CANCELED.getCode());
        order.setCancelReason(request.getReason() != null ? request.getReason() : "管理员取消");
        groupOrderMapper.updateById(order);

        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>()
                        .eq(GroupOrderMember::getGroupOrderId, orderId)
                        .eq(GroupOrderMember::getJoinStatus, "ACTIVE")
        );

        for (GroupOrderMember member : members) {
            if ("PAID".equals(member.getPayStatus()) && member.getRefundAmountTotal().compareTo(BigDecimal.ZERO) <= 0) {
                member.setRefundAmountTotal(member.getPayAmount());
                member.setJoinStatus("CANCELED");
                groupOrderMemberMapper.updateById(member);
            }
        }
    }
}
