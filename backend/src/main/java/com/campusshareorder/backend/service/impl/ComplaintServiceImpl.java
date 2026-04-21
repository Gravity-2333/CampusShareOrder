package com.campusshareorder.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusshareorder.backend.dto.complaint.CreateComplaintRequest;
import com.campusshareorder.backend.entity.Complaint;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.ComplaintMapper;
import com.campusshareorder.backend.mapper.GroupOrderMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.ComplaintService;
import com.campusshareorder.backend.vo.complaint.ComplaintDetailVO;
import com.campusshareorder.backend.vo.complaint.ComplaintListItemVO;
import cn.hutool.core.util.IdUtil;
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
    private final UserAccountMapper userAccountMapper;

    @Override
    @Transactional
    public void createComplaint(CreateComplaintRequest request, Long userId) {
        Complaint complaint = new Complaint();
        complaint.setComplaintNo("CMP" + IdUtil.fastSimpleUUID().substring(0, 12).toUpperCase());
        complaint.setGroupOrderId(request.getOrderId());
        complaint.setComplainantUserId(userId);
        complaint.setAccusedUserId(request.getAccusedUserId());
        complaint.setType(request.getType());
        complaint.setContent(request.getContent());
        complaint.setStatus("PENDING");
        complaintMapper.insert(complaint);
    }

    @Override
    public List<ComplaintListItemVO> getMyComplaints(Long userId) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Complaint::getComplainantUserId, userId)
                .orderByDesc(Complaint::getCreatedAt);
        
        return complaintMapper.selectList(wrapper).stream().map(c -> {
            ComplaintListItemVO vo = new ComplaintListItemVO();
            vo.setId(c.getId());
            vo.setComplaintNo(c.getComplaintNo());
            vo.setType(c.getType());
            vo.setStatus(c.getStatus());
            vo.setCreatedAt(c.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ComplaintDetailVO getComplaintDetail(Long id, Long userId) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            throw new RuntimeException("投诉记录不存在");
        }
        if (!complaint.getComplainantUserId().equals(userId)) {
            throw new RuntimeException("无权查看此投诉");
        }

        ComplaintDetailVO vo = new ComplaintDetailVO();
        vo.setId(complaint.getId());
        vo.setComplaintNo(complaint.getComplaintNo());
        vo.setOrderId(complaint.getGroupOrderId());
        
        GroupOrder order = groupOrderMapper.selectById(complaint.getGroupOrderId());
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());
        }

        UserAccount accusedUser = userAccountMapper.selectById(complaint.getAccusedUserId());
        if (accusedUser != null) {
            vo.setAccusedUserName(accusedUser.getNickname());
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