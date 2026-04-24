package com.campusshareorder.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.enums.UserStatus;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.dto.admin.AdminUserQueryRequest;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.AdminUserService;
import com.campusshareorder.backend.vo.admin.AdminUserItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserAccountMapper userAccountMapper;

    @Override
    public PageResult<AdminUserItemVO> listUsers(AdminUserQueryRequest request) {
        Page<UserAccount> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            wrapper.and(item -> item.like(UserAccount::getPhone, request.getKeyword())
                    .or()
                    .like(UserAccount::getNickname, request.getKeyword())
                    .or()
                    .like(UserAccount::getStudentNo, request.getKeyword()));
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            wrapper.eq(UserAccount::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(UserAccount::getCreatedAt);

        Page<UserAccount> userPage = userAccountMapper.selectPage(page, wrapper);
        List<AdminUserItemVO> list = userPage.getRecords().stream().map(user -> {
            AdminUserItemVO vo = new AdminUserItemVO();
            vo.setUserId(user.getId());
            vo.setPhone(user.getPhone());
            vo.setNickname(user.getNickname());
            vo.setStudentNo(user.getStudentNo());
            vo.setIsVerified(user.getIsVerified());
            vo.setCreditScore(user.getCreditScore());
            vo.setStatus(user.getStatus());
            vo.setCreatedAt(user.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(list, request.getPage(), request.getPageSize(), userPage.getTotal());
    }

    @Override
    @Transactional
    public void banUser(Long userId, Long adminId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        user.setStatus(UserStatus.BANNED.getCode());
        userAccountMapper.updateById(user);
    }

    @Override
    @Transactional
    public void unbanUser(Long userId, Long adminId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        user.setStatus(UserStatus.ACTIVE.getCode());
        userAccountMapper.updateById(user);
    }
}
