package com.campusshareorder.backend.service;

import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.dto.admin.AdminCancelOrderRequest;
import com.campusshareorder.backend.dto.admin.AdminOrderQueryRequest;
import com.campusshareorder.backend.vo.order.OrderDetailVO;
import com.campusshareorder.backend.vo.order.OrderListItemVO;

public interface AdminOrderService {
    PageResult<OrderListItemVO> listOrders(AdminOrderQueryRequest request);
    
    OrderDetailVO getOrderDetail(Long orderId);
    
    void cancelOrder(Long orderId, AdminCancelOrderRequest request, Long adminId);
}
