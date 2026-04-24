package com.campusshareorder.backend.controller.admin;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.common.util.SecurityUtils;
import com.campusshareorder.backend.dto.admin.AdminComplaintQueryRequest;
import com.campusshareorder.backend.dto.admin.AdminHandleComplaintRequest;
import com.campusshareorder.backend.service.AdminComplaintService;
import com.campusshareorder.backend.vo.complaint.ComplaintDetailVO;
import com.campusshareorder.backend.vo.admin.AdminComplaintItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController {

    private final AdminComplaintService adminComplaintService;

    @GetMapping
    public ApiResponse<PageResult<AdminComplaintItemVO>> listComplaints(AdminComplaintQueryRequest request) {
        return ApiResponse.success(adminComplaintService.listComplaints(request));
    }

    @GetMapping("/{complaintId}")
    public ApiResponse<ComplaintDetailVO> getComplaintDetail(@PathVariable Long complaintId) {
        return ApiResponse.success(adminComplaintService.getComplaintDetail(complaintId));
    }

    @PostMapping("/{complaintId}/handle")
    public ApiResponse<Void> handleComplaint(@PathVariable Long complaintId, @RequestBody AdminHandleComplaintRequest request) {
        Long adminId = SecurityUtils.getRequiredCurrentUserId();
        adminComplaintService.handleComplaint(complaintId, request, adminId);
        return ApiResponse.success();
    }
}
