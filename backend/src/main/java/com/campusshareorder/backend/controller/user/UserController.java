package com.campusshareorder.backend.controller.user;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.common.util.SecurityUtils;
import com.campusshareorder.backend.dto.order.MyOrderQueryRequest;
import com.campusshareorder.backend.dto.user.UpdateProfileRequest;
import com.campusshareorder.backend.dto.user.VerifyStudentRequest;
import com.campusshareorder.backend.service.UserService;
import com.campusshareorder.backend.vo.order.MyOrderListItemVO;
import com.campusshareorder.backend.vo.user.UserCreditVO;
import com.campusshareorder.backend.vo.user.UserProfileVO;
import com.campusshareorder.backend.vo.user.VerifyStudentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/verify-student")
    public ApiResponse<VerifyStudentVO> verifyStudent(@Valid @RequestBody VerifyStudentRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(userService.verifyStudent(userId, request));
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> getProfile() {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        userService.updateProfile(userId, request);
        return ApiResponse.success();
    }

    @GetMapping("/credit")
    public ApiResponse<UserCreditVO> getCreditRecords() {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(userService.getCreditRecords(userId));
    }

    @GetMapping("/my-orders")
    public ApiResponse<PageResult<MyOrderListItemVO>> getMyOrders(MyOrderQueryRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(userService.getMyOrders(request, userId));
    }
}
