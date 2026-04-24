package com.campusshareorder.backend.controller.order;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.order.CreateOrderRequest;
import com.campusshareorder.backend.dto.order.JoinOrderRequest;
import com.campusshareorder.backend.dto.order.OrderQueryRequest;
import com.campusshareorder.backend.dto.order.UploadReceiptRequest;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.utils.SecurityUtils;
import com.campusshareorder.backend.vo.common.PageVO;
import com.campusshareorder.backend.vo.order.CreateOrderVO;
import com.campusshareorder.backend.vo.order.OrderDetailVO;
import com.campusshareorder.backend.vo.order.OrderListItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<CreateOrderVO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        return ApiResponse.success(orderService.createOrder(request, userId));
    }

    @GetMapping
    public ApiResponse<PageVO<OrderListItemVO>> getOrderList(OrderQueryRequest request) {
        SecurityUtils.getRequiredUserId();
        return ApiResponse.success(orderService.getOrderList(request));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getRequiredUserId();
        return ApiResponse.success(orderService.getOrderDetail(orderId, userId));
    }

    @PostMapping("/{orderId}/join")
    public ApiResponse<Void> joinOrder(@PathVariable Long orderId, @Valid @RequestBody(required = false) JoinOrderRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        orderService.joinOrder(orderId, request, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<Void> payOrder(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getRequiredUserId();
        orderService.payOrder(orderId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/exit")
    public ApiResponse<Void> exitOrder(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getRequiredUserId();
        orderService.exitOrder(orderId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/upload-receipt")
    public ApiResponse<Void> uploadReceipt(@PathVariable Long orderId, @Valid @RequestBody UploadReceiptRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        orderService.uploadReceipt(orderId, request, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/mark-delivered")
    public ApiResponse<Void> markDelivered(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getRequiredUserId();
        orderService.markDelivered(orderId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/confirm-received")
    public ApiResponse<Void> confirmReceived(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getRequiredUserId();
        orderService.confirmReceived(orderId, userId);
        return ApiResponse.success();
    }
}
