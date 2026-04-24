package com.campusshareorder.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusshareorder.backend.common.enums.ComplaintStatus;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.enums.OrderStatus;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.dto.admin.AdminHandleComplaintRequest;
import com.campusshareorder.backend.dto.admin.AdminComplaintQueryRequest;
import com.campusshareorder.backend.entity.Complaint;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.entity.GroupOrderMember;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.ComplaintMapper;
import com.campusshareorder.backend.mapper.GroupOrderMapper;
import com.campusshareorder.backend.mapper.GroupOrderMemberMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.AdminComplaintService;
import com.campusshareorder.backend.vo.complaint.ComplaintDetailVO;
import com.campusshareorder.backend.vo.admin.AdminComplaintItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminComplaintServiceImpl implements AdminComplaintService {

    private final ComplaintMapper complaintMapper;
    private final GroupOrderMapper groupOrderMapper;
    private final GroupOrderMemberMapper groupOrderMemberMapper;
    private final UserAccountMapper userAccountMapper;

    @Override
    public PageResult<AdminComplaintItemVO> listComplaints(AdminComplaintQueryRequest request) {
        Page<Complaint> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            wrapper.eq(Complaint::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(Complaint::getCreatedAt);

        Page<Complaint> complaintPage = complaintMapper.selectPage(page, wrapper);
        List<AdminComplaintItemVO> list = complaintPage.getRecords().stream().map(complaint -> {
            AdminComplaintItemVO vo = new AdminComplaintItemVO();
            vo.setComplaintId(complaint.getId());
            vo.setComplaintNo(complaint.getComplaintNo());
            vo.setType(complaint.getType());
            vo.setContent(complaint.getContent());
            vo.setStatus(complaint.getStatus());
            vo.setCreatedAt(complaint.getCreatedAt());

            GroupOrder order = groupOrderMapper.selectById(complaint.getGroupOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
                vo.setProductName(order.getProductName());
            }

            UserAccount complainant = userAccountMapper.selectById(complaint.getComplainantUserId());
            if (complainant != null) {
                vo.setComplainantNickname(complainant.getNickname());
            }

            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(list, request.getPage(), request.getPageSize(), complaintPage.getTotal());
    }

    @Override
    public ComplaintDetailVO getComplaintDetail(Long complaintId) {
        Complaint complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            throw new BusinessException(ErrorCode.COMPLAINT_NOT_FOUND, "投诉不存在");
        }

        ComplaintDetailVO vo = new ComplaintDetailVO();
        vo.setComplaintId(complaint.getId());
        vo.setComplaintNo(complaint.getComplaintNo());
        vo.setOrderId(complaint.getGroupOrderId());

        GroupOrder order = groupOrderMapper.selectById(complaint.getGroupOrderId());
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());
            vo.setProductName(order.getProductName());
        }

        UserAccount complainant = userAccountMapper.selectById(complaint.getComplainantUserId());
        if (complainant != null) {
            vo.setComplainantNickname(complainant.getNickname());
        }

         UserAccount accused = userAccountMapper.selectById(complaint.getAccusedUserId());
         if (accused != null) {
             vo.setAccusedNickname(accused.getNickname());
         }

         vo.setType(complaint.getType());
         vo.setContent(complaint.getContent());
         vo.setStatus(complaint.getStatus());
         vo.setHandleResult(complaint.getHandleResult());
         vo.setHandledAt(complaint.getHandledAt());
         vo.setCreatedAt(complaint.getCreatedAt());
         return vo;
    }

    @Override
    @Transactional
    public void handleComplaint(Long complaintId, AdminHandleComplaintRequest request, Long adminId) {
        Complaint complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            throw new BusinessException(ErrorCode.COMPLAINT_NOT_FOUND, "投诉不存在");
        }

        if (!ComplaintStatus.PENDING.getCode().equals(complaint.getStatus())) {
            throw new BusinessException(ErrorCode.COMPLAINT_ALREADY_HANDLED, "投诉已处理");
        }

        GroupOrder order = groupOrderMapper.selectById(complaint.getGroupOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "投诉关联订单不存在");
        }

        // 更新投诉状态
        complaint.setStatus(request.getIsApproved() ? ComplaintStatus.RESOLVED.getCode() : ComplaintStatus.REJECTED.getCode());
        complaint.setHandleResult(request.getResult());
        complaint.setHandledByAdminId(adminId);
        complaint.setHandledAt(java.time.LocalDateTime.now());
        complaintMapper.updateById(complaint);

        // 如果投诉成立，取消订单
        if (request.getIsApproved() && order != null && !OrderStatus.CANCELED.getCode().equals(order.getStatus()) && !OrderStatus.COMPLETED.getCode().equals(order.getStatus())) {
            order.setStatus(OrderStatus.CANCELED.getCode());
            order.setCancelReason("投诉成立，管理员取消");
            order.setComplaintOpened(false);
            groupOrderMapper.updateById(order);

            // 退款给所有支付过的成员
            List<GroupOrderMember> members = groupOrderMemberMapper.selectList(
                    new LambdaQueryWrapper<GroupOrderMember>()
                            .eq(GroupOrderMember::getGroupOrderId, order.getId())
                            .eq(GroupOrderMember::getJoinStatus, "ACTIVE")
            );

            for (GroupOrderMember member : members) {
                if ("PAID".equals(member.getPayStatus()) && member.getRefundAmountTotal().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    member.setRefundAmountTotal(member.getPayAmount());
                    member.setJoinStatus("CANCELED");
                    groupOrderMemberMapper.updateById(member);
                }
            }
        }
    }
}
