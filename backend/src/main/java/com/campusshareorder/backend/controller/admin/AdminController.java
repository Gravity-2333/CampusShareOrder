package com.campusshareorder.backend.controller.admin;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.admin.BanUserRequest;
import com.campusshareorder.backend.dto.admin.CancelOrderRequest;
import com.campusshareorder.backend.dto.admin.HandleComplaintRequest;
import com.campusshareorder.backend.service.AdminService;
import com.campusshareorder.backend.vo.admin.AdminCapitalRecordVO;
import com.campusshareorder.backend.vo.admin.AdminComplaintDetailVO;
import com.campusshareorder.backend.vo.admin.AdminOperationLogVO;
import com.campusshareorder.backend.vo.admin.AdminUserDetailVO;
import com.campusshareorder.backend.vo.admin.AdminUserListItemVO;
import com.campusshareorder.backend.vo.common.PageVO;
import com.campusshareorder.backend.vo.complaint.ComplaintListItemVO;
import com.campusshareorder.backend.vo.order.OrderDetailVO;
import com.campusshareorder.backend.vo.order.OrderListItemVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ApiResponse<PageVO<AdminUserListItemVO>> getUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getUsers(keyword, status, page, pageSize));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<AdminUserDetailVO> getUserDetail(@PathVariable Long userId, HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getUserDetail(userId));
    }

    @PostMapping("/users/{userId}/ban")
    public ApiResponse<Void> banUser(@PathVariable Long userId, @RequestBody(required = false) BanUserRequest body, HttpServletRequest request) {
        Long adminId = requireAdmin(request);
        adminService.banUser(userId, body, adminId);
        return ApiResponse.success();
    }

    @PostMapping("/users/{userId}/unban")
    public ApiResponse<Void> unbanUser(@PathVariable Long userId, HttpServletRequest request) {
        Long adminId = requireAdmin(request);
        adminService.unbanUser(userId, adminId);
        return ApiResponse.success();
    }

    @GetMapping("/orders")
    public ApiResponse<PageVO<OrderListItemVO>> getOrders(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getOrders(status, page, pageSize));
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderDetailVO> getOrderDetail(@PathVariable Long orderId, HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getOrderDetail(orderId));
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Long orderId, @RequestBody(required = false) CancelOrderRequest body, HttpServletRequest request) {
        Long adminId = requireAdmin(request);
        adminService.cancelOrder(orderId, body, adminId);
        return ApiResponse.success();
    }

    @GetMapping("/complaints")
    public ApiResponse<PageVO<ComplaintListItemVO>> getComplaints(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getComplaints(page, pageSize));
    }

    @GetMapping("/complaints/{complaintId}")
    public ApiResponse<AdminComplaintDetailVO> getComplaintDetail(@PathVariable Long complaintId, HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getComplaintDetail(complaintId));
    }

    @PostMapping("/complaints/{complaintId}/handle")
    public ApiResponse<Void> handleComplaint(@PathVariable Long complaintId, @RequestBody(required = false) HandleComplaintRequest body, HttpServletRequest request) {
        Long adminId = requireAdmin(request);
        adminService.handleComplaint(complaintId, body, adminId);
        return ApiResponse.success();
    }

    @GetMapping("/records/capital")
    public ApiResponse<PageVO<AdminCapitalRecordVO>> getCapitalRecords(
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getCapitalRecords(type, page, pageSize));
    }

    @GetMapping("/records/logs")
    public ApiResponse<PageVO<AdminOperationLogVO>> getOperationLogs(
            @RequestParam(defaultValue = "") String action,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(adminService.getOperationLogs(action, page, pageSize));
    }

    private Long requireAdmin(HttpServletRequest request) {
        String role = request.getAttribute("role") == null ? "" : String.valueOf(request.getAttribute("role"));
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("当前账号无权访问管理端接口");
        }

        Object userId = request.getAttribute("userId");
        if (userId instanceof Long longValue) {
            return longValue;
        }

        throw new RuntimeException("管理员未登录");
    }
}
