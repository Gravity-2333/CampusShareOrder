package com.campusshareorder.backend.vo.admin;

import com.campusshareorder.backend.vo.complaint.ComplaintListItemVO;
import com.campusshareorder.backend.vo.order.OrderListItemVO;
import lombok.Data;

import java.util.List;

@Data
public class AdminDashboardOverviewVO {
    private AdminDashboardMetricsVO metrics;
    private List<OrderListItemVO> recentOrders;
    private List<ComplaintListItemVO> recentComplaints;
    private List<AdminOperationLogVO> recentLogs;
}
