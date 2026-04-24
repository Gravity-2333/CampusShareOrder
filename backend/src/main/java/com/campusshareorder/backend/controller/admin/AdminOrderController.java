package com.campusshareorder.backend.controller.admin;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.common.util.SecurityUtils;
import com.campusshareorder.backend.dto.admin.AdminOrderQueryRequest;
import com.campusshareorder.backend.dto.admin.AdminCancelOrderRequest;
import com.campusshareorder.backend.service.AdminOrderService;
import com.campusshareorder.backend.vo.order.OrderDetailVO;
import com.campusshareorder.backend.vo.order.OrderListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ApiResponse<PageResult<OrderListItemVO>> listOrders(AdminOrderQueryRequest request) {
        return ApiResponse.success(adminOrderService.listOrders(request));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        Long adminId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(adminOrderService.getOrderDetail(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Long orderId, @RequestBody AdminCancelOrderRequest request) {
        Long adminId = SecurityUtils.getRequiredCurrentUserId();
        adminOrderService.cancelOrder(orderId, request, adminId);
        return ApiResponse.success();
    }
}
