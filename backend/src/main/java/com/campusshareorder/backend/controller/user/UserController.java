package com.campusshareorder.backend.controller.user;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.vo.order.MyOrderListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final OrderService orderService;

    @GetMapping("/my-orders")
    public ApiResponse<List<MyOrderListItemVO>> getMyOrders() {
        // 暂时硬编码用户ID，实际应该从JWT中获取
        Long userId = 1L;
        List<MyOrderListItemVO> list = orderService.getMyOrders(userId);
        return ApiResponse.success(list);
    }
}