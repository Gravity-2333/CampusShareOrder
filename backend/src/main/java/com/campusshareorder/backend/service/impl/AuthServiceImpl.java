package com.campusshareorder.backend.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusshareorder.backend.dto.auth.AdminLoginRequest;
import com.campusshareorder.backend.dto.auth.UserLoginRequest;
import com.campusshareorder.backend.dto.auth.UserRegisterRequest;
import com.campusshareorder.backend.entity.AdminAccount;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.AdminAccountMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.AuthService;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.common.enums.UserStatus;
import com.campusshareorder.backend.common.util.JwtUtil;
import com.campusshareorder.backend.vo.auth.AdminLoginInfoVO;
import com.campusshareorder.backend.vo.auth.AdminLoginVO;
import com.campusshareorder.backend.vo.auth.CurrentLoginInfoVO;
import com.campusshareorder.backend.vo.auth.LoginUserInfoVO;
import com.campusshareorder.backend.vo.auth.RegisterResultVO;
import com.campusshareorder.backend.vo.auth.UserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminAccountMapper adminAccountMapper;
    private final UserAccountMapper userAccountMapper;
    private final JwtUtil jwtUtil;

    @Override
    public RegisterResultVO register(UserRegisterRequest request) {
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getPhone, request.getPhone());
        if (userAccountMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "该手机号已注册");
        }

        UserAccount user = new UserAccount();
        user.setPhone(request.getPhone());
        user.setPasswordHash(BCrypt.hashpw(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setCreditScore(80);
        user.setIsVerified(false);
        user.setStatus(UserStatus.ACTIVE.getCode());
        userAccountMapper.insert(user);

        RegisterResultVO vo = new RegisterResultVO();
        vo.setUserId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setRole("USER");
        vo.setStatus(user.getStatus());
        vo.setIsVerified(user.getIsVerified());
        vo.setCreditScore(user.getCreditScore());
        return vo;
    }

    @Override
    public UserLoginVO login(UserLoginRequest request) {
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getPhone, request.getPhone());
        UserAccount user = userAccountMapper.selectOne(wrapper);

        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "手机号或密码错误");
        }

        if (UserStatus.BANNED.getCode().equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_BANNED, "账号已被封禁，请联系管理员");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), "USER");

        LoginUserInfoVO userInfo = new LoginUserInfoVO();
        userInfo.setUserId(user.getId());
        userInfo.setPhone(user.getPhone());
        userInfo.setNickname(user.getNickname());
        userInfo.setStudentNo(user.getStudentNo());
        userInfo.setIsVerified(user.getIsVerified());
        userInfo.setRole("USER");
        userInfo.setStatus(user.getStatus());
        userInfo.setCreditScore(user.getCreditScore());
        userInfo.setContactInfo(user.getContactInfo());

        UserLoginVO vo = new UserLoginVO();
        vo.setToken(token);
        vo.setUserInfo(userInfo);
        return vo;
    }

    @Override
    public AdminLoginVO adminLogin(AdminLoginRequest request) {
        LambdaQueryWrapper<AdminAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminAccount::getUsername, request.getUsername());
        AdminAccount admin = adminAccountMapper.selectOne(wrapper);

        if (admin == null || !BCrypt.checkpw(request.getPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "管理员账号或密码错误");
        }

        if (UserStatus.BANNED.getCode().equals(admin.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_BANNED, "管理员账号已被禁用");
        }

        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), "ADMIN");

        AdminLoginInfoVO adminInfo = new AdminLoginInfoVO();
        adminInfo.setAdminId(admin.getId());
        adminInfo.setUsername(admin.getUsername());
        adminInfo.setRole("ADMIN");
        adminInfo.setStatus(admin.getStatus());

        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(token);
        vo.setAdminInfo(adminInfo);
        return vo;
    }

    @Override
    public CurrentLoginInfoVO getCurrentInfo(Long currentId, String role) {
        if ("ADMIN".equals(role)) {
            AdminAccount admin = adminAccountMapper.selectById(currentId);
         if (admin == null) {
             throw new BusinessException(ErrorCode.UNAUTHORIZED, "管理员不存在");
         }

            CurrentLoginInfoVO vo = new CurrentLoginInfoVO();
            vo.setRole("ADMIN");
            vo.setAdminId(admin.getId());
            vo.setUsername(admin.getUsername());
            vo.setStatus(admin.getStatus());
            return vo;
        }

         UserAccount user = userAccountMapper.selectById(currentId);
         if (user == null) {
             throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
         }

        CurrentLoginInfoVO vo = new CurrentLoginInfoVO();
        vo.setRole("USER");
        vo.setUserId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setStudentNo(user.getStudentNo());
        vo.setIsVerified(user.getIsVerified());
        vo.setStatus(user.getStatus());
        vo.setCreditScore(user.getCreditScore());
        vo.setContactInfo(user.getContactInfo());
        return vo;
    }
}
