package com.campusshareorder.backend.controller.auth;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.auth.UserLoginRequest;
import com.campusshareorder.backend.dto.auth.UserRegisterRequest;
import com.campusshareorder.backend.service.AuthService;
import com.campusshareorder.backend.utils.SecurityUtils;
import com.campusshareorder.backend.vo.auth.CurrentLoginInfoVO;
import com.campusshareorder.backend.vo.auth.RegisterResultVO;
import com.campusshareorder.backend.vo.auth.UserLoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<RegisterResultVO> register(@Valid @RequestBody UserRegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<UserLoginVO> login(@Valid @RequestBody UserLoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentLoginInfoVO> getCurrentInfo() {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(authService.getCurrentInfo(userId));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        // JWT 是无状态的，登出主要由前端清除 Token 实现
        return ApiResponse.success();
    }
}
