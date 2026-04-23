package com.campusshareorder.backend.service;

import com.campusshareorder.backend.dto.auth.UserLoginRequest;
import com.campusshareorder.backend.dto.auth.UserRegisterRequest;
import com.campusshareorder.backend.vo.auth.CurrentLoginInfoVO;
import com.campusshareorder.backend.vo.auth.RegisterResultVO;
import com.campusshareorder.backend.vo.auth.UserLoginVO;

public interface AuthService {
    RegisterResultVO register(UserRegisterRequest request);
    UserLoginVO login(UserLoginRequest request);
    CurrentLoginInfoVO getCurrentInfo(Long userId);
}
