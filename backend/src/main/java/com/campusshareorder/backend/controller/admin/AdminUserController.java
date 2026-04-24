package com.campusshareorder.backend.controller.admin;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.common.util.SecurityUtils;
import com.campusshareorder.backend.dto.admin.AdminUserQueryRequest;
import com.campusshareorder.backend.service.AdminUserService;
import com.campusshareorder.backend.vo.admin.AdminUserItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<PageResult<AdminUserItemVO>> listUsers(AdminUserQueryRequest request) {
        return ApiResponse.success(adminUserService.listUsers(request));
    }

    @PostMapping("/{userId}/ban")
    public ApiResponse<Void> banUser(@PathVariable Long userId) {
        Long adminId = SecurityUtils.getRequiredCurrentUserId();
        adminUserService.banUser(userId, adminId);
        return ApiResponse.success();
    }

    @PostMapping("/{userId}/unban")
    public ApiResponse<Void> unbanUser(@PathVariable Long userId) {
        Long adminId = SecurityUtils.getRequiredCurrentUserId();
        adminUserService.unbanUser(userId, adminId);
        return ApiResponse.success();
    }
}
