package com.campusshareorder.backend.controller.order;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.order.CreateOrderRequest;
import com.campusshareorder.backend.dto.order.JoinOrderRequest;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.vo.order.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<CreateOrderVO> createOrder(@RequestBody CreateOrderRequest request) {
        // 暂时硬编码用户ID，实际应该从JWT中获取
        Long userId = 1L;
        CreateOrderVO vo = orderService.createOrder(request, userId);
        return ApiResponse.success(vo);
    }

    @GetMapping
    public ApiResponse<List<OrderListItemVO>> getOrderList() {
        List<OrderListItemVO> list = orderService.getOrderList();
        return ApiResponse.success(list);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        // 暂时硬编码用户ID，实际应该从JWT中获取
        Long userId = 1L;
        OrderDetailVO detail = orderService.getOrderDetail(orderId, userId);
        return ApiResponse.success(detail);
    }

    @PostMapping("/{orderId}/join")
    public ApiResponse<Void> joinOrder(@PathVariable Long orderId, @RequestBody JoinOrderRequest request) {
        // 暂时硬编码用户ID，实际应该从JWT中获取
        Long userId = 1L;
        orderService.joinOrder(orderId, request, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<Void> payOrder(@PathVariable Long orderId) {
        // 暂时硬编码用户ID，实际应该从JWT中获取
        Long userId = 1L;
        orderService.payOrder(orderId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/exit")
    public ApiResponse<Void> exitOrder(@PathVariable Long orderId) {
        // 暂时硬编码用户ID，实际应该从JWT中获取
        Long userId = 1L;
        orderService.exitOrder(orderId, userId);
        return ApiResponse.success();
    }
}