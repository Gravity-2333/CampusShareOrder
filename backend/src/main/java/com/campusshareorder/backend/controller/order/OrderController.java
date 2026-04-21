package com.campusshareorder.backend.controller.order;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.order.CreateOrderRequest;
import com.campusshareorder.backend.dto.order.JoinOrderRequest;
import com.campusshareorder.backend.dto.order.UploadReceiptRequest;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.utils.SecurityUtils;
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
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        CreateOrderVO vo = orderService.createOrder(request, userId);
        return ApiResponse.success(vo);
    }

    @GetMapping
    public ApiResponse<List<OrderListItemVO>> getOrderList() {
        List<OrderListItemVO> list = orderService.getOrderList();
        return ApiResponse.success(list);
    }

    @GetMapping("/my")
    public ApiResponse<List<MyOrderListItemVO>> getMyOrders() {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        List<MyOrderListItemVO> list = orderService.getMyOrders(userId);
        return ApiResponse.success(list);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        OrderDetailVO detail = orderService.getOrderDetail(orderId, userId);
        return ApiResponse.success(detail);
    }

    @PostMapping("/{orderId}/join")
    public ApiResponse<Void> joinOrder(@PathVariable Long orderId, @RequestBody JoinOrderRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        orderService.joinOrder(orderId, request, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<Void> payOrder(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        orderService.payOrder(orderId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/exit")
    public ApiResponse<Void> exitOrder(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        orderService.exitOrder(orderId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/receipt")
    public ApiResponse<Void> uploadReceipt(@PathVariable Long orderId, @RequestBody UploadReceiptRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        orderService.uploadReceipt(orderId, request, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/deliver")
    public ApiResponse<Void> markDelivered(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        orderService.markDelivered(orderId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/receive")
    public ApiResponse<Void> confirmReceived(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        orderService.confirmReceived(orderId, userId);
        return ApiResponse.success();
    }
}