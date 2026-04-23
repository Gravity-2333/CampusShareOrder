package com.campusshareorder.backend.controller.order;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.order.CreateOrderRequest;
import com.campusshareorder.backend.dto.order.JoinOrderRequest;
import com.campusshareorder.backend.dto.order.OrderQueryRequest;
import com.campusshareorder.backend.dto.order.UploadReceiptRequest;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.vo.order.*;
import com.campusshareorder.backend.vo.common.PageVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
            return ApiResponse.error(401, "请先登录");
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
            return ApiResponse.error(401, "请先登录");
        }
        OrderDetailVO detail = orderService.getOrderDetail(orderId, userId);
        return ApiResponse.success(detail);
    }

    @PostMapping("/{orderId}/join")
    public ApiResponse<Void> joinOrder(@PathVariable Long orderId, @RequestBody JoinOrderRequest request, HttpServletRequest httpRequest) {
        // 从 JWT 中获取用户ID
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }
        orderService.joinOrder(orderId, request, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<Void> payOrder(@PathVariable Long orderId, HttpServletRequest httpRequest) {
        // 从 JWT 中获取用户ID
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }
        orderService.payOrder(orderId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/exit")
    public ApiResponse<Void> exitOrder(@PathVariable Long orderId, HttpServletRequest httpRequest) {
        // 从 JWT 中获取用户ID
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }
        orderService.exitOrder(orderId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/receipt")
    public ApiResponse<Void> uploadReceipt(@PathVariable Long orderId, @RequestBody UploadReceiptRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }
        orderService.uploadReceipt(orderId, request, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/deliver")
    public ApiResponse<Void> markDelivered(@PathVariable Long orderId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }
        orderService.markDelivered(orderId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/receive")
    public ApiResponse<Void> confirmReceived(@PathVariable Long orderId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }
        orderService.confirmReceived(orderId, userId);
        return ApiResponse.success();
    }
}