package com.campusshareorder.backend.vo.admin;

import lombok.Data;

@Data
public class AdminDashboardMetricsVO {
    private Long users;
    private Long orders;
    private Long complaints;
    private Long completedOrders;
    private Long pendingComplaints;
    private Long todayNewUsers;
}
