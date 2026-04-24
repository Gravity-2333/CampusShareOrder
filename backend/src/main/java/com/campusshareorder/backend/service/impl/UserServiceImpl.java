package com.campusshareorder.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.dto.order.MyOrderQueryRequest;
import com.campusshareorder.backend.dto.user.UpdateProfileRequest;
import com.campusshareorder.backend.dto.user.VerifyStudentRequest;
import com.campusshareorder.backend.entity.CreditChangeRecord;
import com.campusshareorder.backend.entity.GroupOrderMember;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.CreditChangeRecordMapper;
import com.campusshareorder.backend.mapper.GroupOrderMemberMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.service.UserService;
import com.campusshareorder.backend.vo.order.MyOrderListItemVO;
import com.campusshareorder.backend.vo.user.UserCreditItemVO;
import com.campusshareorder.backend.vo.user.UserCreditVO;
import com.campusshareorder.backend.vo.user.UserProfileVO;
import com.campusshareorder.backend.vo.user.VerifyStudentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountMapper userAccountMapper;
    private final CreditChangeRecordMapper creditChangeRecordMapper;
    private final GroupOrderMemberMapper groupOrderMemberMapper;
    private final OrderService orderService;

    @Override
    @Transactional
    public VerifyStudentVO verifyStudent(Long userId, VerifyStudentRequest request) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }

        user.setStudentNo(request.getStudentNo());
        user.setIsVerified(true);
        userAccountMapper.updateById(user);

        VerifyStudentVO vo = new VerifyStudentVO();
        vo.setIsVerified(true);
        vo.setStudentNo(user.getStudentNo());
        return vo;
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }

        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setStudentNo(user.getStudentNo());
        vo.setIsVerified(user.getIsVerified());
        vo.setCreditScore(user.getCreditScore());
        vo.setStatus(user.getStatus());
        vo.setContactInfo(user.getContactInfo());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }

        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getContactInfo() != null) {
            user.setContactInfo(request.getContactInfo());
        }
        userAccountMapper.updateById(user);
    }

    @Override
    public UserCreditVO getCreditRecords(Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }

        LambdaQueryWrapper<CreditChangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditChangeRecord::getUserId, userId)
                .orderByDesc(CreditChangeRecord::getCreatedAt);
        List<CreditChangeRecord> records = creditChangeRecordMapper.selectList(wrapper);

        List<UserCreditItemVO> items = records.stream().map(record -> {
            UserCreditItemVO vo = new UserCreditItemVO();
            vo.setRecordId(record.getId());
            vo.setDelta(record.getChangeValue());
            vo.setCurrentScore(user.getCreditScore());
            vo.setChangeReason(record.getRemark());
            vo.setReasonType(record.getReasonType());
            vo.setRemark(record.getRemark());
            vo.setRelatedOrderId(record.getRelatedOrderId());
            vo.setRelatedComplaintId(record.getRelatedComplaintId());
            vo.setCreatedAt(record.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());

        UserCreditVO creditVO = new UserCreditVO();
        creditVO.setCreditScore(user.getCreditScore());
        creditVO.setRecords(items);
        return creditVO;
    }

    @Override
    public PageResult<MyOrderListItemVO> getMyOrders(MyOrderQueryRequest request, Long userId) {
        return orderService.getMyOrders(request, userId);
    }
}
