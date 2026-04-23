package com.campusshareorder.backend.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusshareorder.backend.dto.auth.UserLoginRequest;
import com.campusshareorder.backend.dto.auth.UserRegisterRequest;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.AuthService;
import com.campusshareorder.backend.utils.JwtUtil;
import com.campusshareorder.backend.vo.auth.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAccountMapper userAccountMapper;
    private final JwtUtil jwtUtil;

    @Override
    public RegisterResultVO register(UserRegisterRequest request) {
        // 检查手机号是否已存在
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getPhone, request.getPhone());
        if (userAccountMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该手机号已注册");
        }

        // 创建新用户
        UserAccount user = new UserAccount();
        user.setPhone(request.getPhone());
        user.setPasswordHash(BCrypt.hashpw(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setCreditScore(80); // 默认信用分
        user.setIsVerified(false);
        user.setStatus("NORMAL");
        
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
        // 查找用户
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getPhone, request.getPhone());
        UserAccount user = userAccountMapper.selectOne(wrapper);

        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("手机号或密码错误");
        }

        if ("BANNED".equals(user.getStatus())) {
            throw new RuntimeException("账号已被封禁，请联系管理员");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), "USER");

        UserLoginVO vo = new UserLoginVO();
        vo.setToken(token);
        
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
        vo.setUserInfo(userInfo);

        return vo;
    }

    @Override
    public CurrentLoginInfoVO getCurrentInfo(Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
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
