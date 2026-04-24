package com.campusshareorder.backend.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.dto.complaint.CreateComplaintRequest;
import com.campusshareorder.backend.dto.complaint.MyComplaintQueryRequest;
import com.campusshareorder.backend.entity.Complaint;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.entity.GroupOrderMember;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.ComplaintMapper;
import com.campusshareorder.backend.mapper.GroupOrderMapper;
import com.campusshareorder.backend.mapper.GroupOrderMemberMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.ComplaintService;
import com.campusshareorder.backend.vo.common.PageVO;
import com.campusshareorder.backend.vo.complaint.ComplaintDetailVO;
import com.campusshareorder.backend.vo.complaint.ComplaintListItemVO;
import com.campusshareorder.backend.vo.complaint.CreateComplaintVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint> implements ComplaintService {

    private final ComplaintMapper complaintMapper;
    private final GroupOrderMapper groupOrderMapper;
    private final GroupOrderMemberMapper groupOrderMemberMapper;
    private final UserAccountMapper userAccountMapper;

    @Override
    @Transactional
    public CreateComplaintVO createComplaint(CreateComplaintRequest request, Long userId) {
        GroupOrder order = groupOrderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        Long accusedUserId = request.getAccusedUserId();
        if (accusedUserId == null) {
            accusedUserId = order.getCreatorUserId();
        }

        if (userId.equals(accusedUserId)) {
            throw new BusinessException(ErrorCode.COMPLAINT_SELF_NOT_ALLOWED);
        }

        LambdaQueryWrapper<GroupOrderMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupOrderMember::getGroupOrderId, request.getOrderId())
                .eq(GroupOrderMember::getUserId, accusedUserId);
        if (groupOrderMemberMapper.selectCount(memberWrapper) == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "被投诉人不在该订单中");
        }

        LambdaQueryWrapper<Complaint> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(Complaint::getGroupOrderId, request.getOrderId())
                .eq(Complaint::getComplainantUserId, userId)
                .eq(Complaint::getAccusedUserId, accusedUserId);
        if (complaintMapper.selectCount(duplicateWrapper) > 0) {
            throw new BusinessException(ErrorCode.COMPLAINT_DUPLICATED);
        }

        Complaint complaint = new Complaint();
        complaint.setComplaintNo("CMP" + IdUtil.fastSimpleUUID().substring(0, 12).toUpperCase());
        complaint.setGroupOrderId(request.getOrderId());
        complaint.setComplainantUserId(userId);
        complaint.setAccusedUserId(accusedUserId);
        complaint.setType(request.getType());
        complaint.setContent(request.getContent());
        complaint.setStatus("PENDING");
        complaintMapper.insert(complaint);

        CreateComplaintVO vo = new CreateComplaintVO();
        vo.setComplaintId(complaint.getId());
        vo.setComplaintNo(complaint.getComplaintNo());
        vo.setStatus(complaint.getStatus());
        return vo;
    }

    @Override
    public PageVO<ComplaintListItemVO> getMyComplaints(MyComplaintQueryRequest request, Long userId) {
        Page<Complaint> page = this.page(
                new Page<>(request.getPage(), request.getPageSize()),
                new LambdaQueryWrapper<Complaint>()
                        .eq(Complaint::getComplainantUserId, userId)
                        .orderByDesc(Complaint::getCreatedAt)
        );

        List<ComplaintListItemVO> list = page.getRecords().stream().map(complaint -> {
            ComplaintListItemVO vo = new ComplaintListItemVO();
            vo.setComplaintId(complaint.getId());
            vo.setComplaintNo(complaint.getComplaintNo());
            vo.setType(complaint.getType());
            vo.setStatus(complaint.getStatus());
            vo.setContent(complaint.getContent());
            vo.setCreatedAt(complaint.getCreatedAt());
            vo.setHandledAt(complaint.getHandledAt());
            vo.setHandleResult(complaint.getHandleResult());
            vo.setOpenedBySystem(false);

            UserAccount accusedUser = userAccountMapper.selectById(complaint.getAccusedUserId());
            if (accusedUser != null) {
                vo.setAccusedUserId(accusedUser.getId());
                vo.setAccusedNickname(accusedUser.getNickname());
            }

            GroupOrder order = groupOrderMapper.selectById(complaint.getGroupOrderId());
            if (order != null) {
                vo.setOrderId(order.getId());
                vo.setOrderNo(order.getOrderNo());
                vo.setProductName(order.getProductName());
            }
            return vo;
        }).collect(Collectors.toList());

        return PageVO.of(list, page.getTotal(), (long) request.getPage(), (long) request.getPageSize());
    }

    @Override
    public ComplaintDetailVO getComplaintDetail(Long id, Long userId) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            throw new BusinessException(ErrorCode.COMPLAINT_NOT_FOUND);
        }
        if (!complaint.getComplainantUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看此投诉");
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

        UserAccount accusedUser = userAccountMapper.selectById(complaint.getAccusedUserId());
        if (accusedUser != null) {
            vo.setAccusedNickname(accusedUser.getNickname());
        }

        vo.setType(complaint.getType());
        vo.setContent(complaint.getContent());
        vo.setStatus(complaint.getStatus());
        vo.setHandleResult(complaint.getHandleResult());
        vo.setHandledAt(complaint.getHandledAt());
        vo.setCreatedAt(complaint.getCreatedAt());
        return vo;
    }
}
