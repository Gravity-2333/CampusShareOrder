package com.campusshareorder.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusshareorder.backend.dto.order.CreateOrderRequest;
import com.campusshareorder.backend.dto.order.JoinOrderRequest;
import com.campusshareorder.backend.dto.order.UploadReceiptRequest;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.vo.order.*;

import java.util.List;

public interface OrderService extends IService<GroupOrder> {
    CreateOrderVO createOrder(CreateOrderRequest request, Long userId);
    List<OrderListItemVO> getOrderList();
    OrderDetailVO getOrderDetail(Long orderId, Long userId);
    List<MyOrderListItemVO> getMyOrders(Long userId);
    void joinOrder(Long orderId, JoinOrderRequest request, Long userId);

    void payOrder(Long orderId, Long userId);
    void exitOrder(Long orderId, Long userId);
    void uploadReceipt(Long orderId, UploadReceiptRequest request, Long userId);
    void markDelivered(Long orderId, Long userId);
    void confirmReceived(Long orderId, Long userId);
    void processAutoGroup();
    void processTimeoutCancel();
}