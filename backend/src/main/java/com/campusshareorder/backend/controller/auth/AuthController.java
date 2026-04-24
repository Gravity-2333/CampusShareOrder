package com.campusshareorder.backend.controller.auth;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.auth.AdminLoginRequest;
import com.campusshareorder.backend.dto.auth.UserLoginRequest;
import com.campusshareorder.backend.dto.auth.UserRegisterRequest;
import com.campusshareorder.backend.service.AuthService;
import com.campusshareorder.backend.common.util.SecurityUtils;
import com.campusshareorder.backend.vo.auth.AdminLoginVO;
import com.campusshareorder.backend.vo.auth.CurrentLoginInfoVO;
import com.campusshareorder.backend.vo.auth.RegisterResultVO;
import com.campusshareorder.backend.vo.auth.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/admin/login")
    public ApiResponse<AdminLoginVO> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(authService.adminLogin(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentLoginInfoVO> getCurrentInfo(HttpServletRequest request) {
        Long currentId = SecurityUtils.getRequiredCurrentUserId();
        String role = request.getAttribute("role") == null ? "USER" : String.valueOf(request.getAttribute("role"));
        return ApiResponse.success(authService.getCurrentInfo(currentId, role));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }
}
