package com.campusshareorder.backend.controller.order;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.order.CreateOrderRequest;
import com.campusshareorder.backend.dto.order.JoinOrderRequest;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.vo.order.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<CreateOrderVO> createOrder(@RequestBody CreateOrderRequest request, HttpServletRequest httpRequest) {
        // 从 JWT 中获取用户ID
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        CreateOrderVO vo = orderService.createOrder(request, userId);
        return ApiResponse.success(vo);
    }

    @GetMapping
    public ApiResponse<PageVO<OrderListItemVO>> getOrderList(OrderQueryRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageVO<OrderListItemVO> page = orderService.getOrderList(request);
        return ApiResponse.success(page);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailVO> getOrderDetail(@PathVariable Long orderId, HttpServletRequest httpRequest) {
        // 从 JWT 中获取用户ID
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        OrderDetailVO detail = orderService.getOrderDetail(orderId, userId);
        return ApiResponse.success(detail);
    }

    @PostMapping("/{orderId}/join")
    public ApiResponse<Void> joinOrder(@PathVariable Long orderId, @RequestBody JoinOrderRequest request, HttpServletRequest httpRequest) {
        // 从 JWT 中获取用户ID
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        orderService.joinOrder(orderId, request, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<Void> payOrder(@PathVariable Long orderId, HttpServletRequest httpRequest) {
        // 从 JWT 中获取用户ID
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        orderService.payOrder(orderId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/exit")
    public ApiResponse<Void> exitOrder(@PathVariable Long orderId, HttpServletRequest httpRequest) {
        // 从 JWT 中获取用户ID
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        orderService.exitOrder(orderId, userId);
        return ApiResponse.success();
    }
}

    @GetMapping
    public ApiResponse<PageVO<OrderListItemVO>> getOrderList(OrderQueryRequest request) {
        PageVO<OrderListItemVO> page = orderService.getOrderList(request);
        return ApiResponse.success(page);
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