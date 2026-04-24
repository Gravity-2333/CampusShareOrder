package com.campusshareorder.backend.service;

import com.campusshareorder.backend.dto.admin.BanUserRequest;
import com.campusshareorder.backend.dto.admin.CancelOrderRequest;
import com.campusshareorder.backend.dto.admin.HandleComplaintRequest;
import com.campusshareorder.backend.vo.admin.AdminCapitalRecordVO;
import com.campusshareorder.backend.vo.admin.AdminComplaintDetailVO;
import com.campusshareorder.backend.vo.admin.AdminDashboardOverviewVO;
import com.campusshareorder.backend.vo.admin.AdminOperationLogVO;
import com.campusshareorder.backend.vo.admin.AdminUserDetailVO;
import com.campusshareorder.backend.vo.admin.AdminUserListItemVO;
import com.campusshareorder.backend.vo.common.PageVO;
import com.campusshareorder.backend.vo.complaint.ComplaintListItemVO;
import com.campusshareorder.backend.vo.order.OrderDetailVO;
import com.campusshareorder.backend.vo.order.OrderListItemVO;

public interface AdminService {
    AdminDashboardOverviewVO getDashboardOverview();

    PageVO<AdminUserListItemVO> getUsers(String keyword, String status, Integer page, Integer pageSize);

    AdminUserDetailVO getUserDetail(Long userId);

    void banUser(Long userId, BanUserRequest request, Long adminId);

    void unbanUser(Long userId, Long adminId);

    PageVO<OrderListItemVO> getOrders(String keyword, String status, Integer page, Integer pageSize);

    OrderDetailVO getOrderDetail(Long orderId);

    void cancelOrder(Long orderId, CancelOrderRequest request, Long adminId);

    PageVO<ComplaintListItemVO> getComplaints(String status, Integer page, Integer pageSize);

    AdminComplaintDetailVO getComplaintDetail(Long complaintId);

    void handleComplaint(Long complaintId, HandleComplaintRequest request, Long adminId);

    PageVO<AdminCapitalRecordVO> getCapitalRecords(String type, Integer page, Integer pageSize);

    PageVO<AdminOperationLogVO> getOperationLogs(String action, Integer page, Integer pageSize);
}
