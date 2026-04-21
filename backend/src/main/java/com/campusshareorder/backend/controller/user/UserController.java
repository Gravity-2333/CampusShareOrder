package com.campusshareorder.backend.controller.user;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.user.VerifyStudentRequest;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.utils.SecurityUtils;
import com.campusshareorder.backend.vo.order.MyOrderListItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final OrderService orderService;
    private final UserAccountMapper userAccountMapper;

    @GetMapping("/my-orders")
    public ApiResponse<List<MyOrderListItemVO>> getMyOrders() {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        List<MyOrderListItemVO> list = orderService.getMyOrders(userId);
        return ApiResponse.success(list);
    }

    @PostMapping("/verify-student")
    public ApiResponse<Void> verifyStudent(@Valid @RequestBody VerifyStudentRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStudentNo(request.getStudentNo());
        user.setIsVerified(true);
        userAccountMapper.updateById(user);
        
        return ApiResponse.success();
    }
}