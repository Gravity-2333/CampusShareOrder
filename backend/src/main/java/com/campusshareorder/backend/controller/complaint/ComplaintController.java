package com.campusshareorder.backend.controller.complaint;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.complaint.CreateComplaintRequest;
import com.campusshareorder.backend.service.ComplaintService;
import com.campusshareorder.backend.utils.SecurityUtils;
import com.campusshareorder.backend.vo.complaint.ComplaintDetailVO;
import com.campusshareorder.backend.vo.complaint.ComplaintListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ApiResponse<Void> createComplaint(@RequestBody CreateComplaintRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        complaintService.createComplaint(request, userId);
        return ApiResponse.success();
    }

    @GetMapping("/my")
    public ApiResponse<List<ComplaintListItemVO>> getMyComplaints() {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        List<ComplaintListItemVO> list = complaintService.getMyComplaints(userId);
        return ApiResponse.success(list);
    }

    @GetMapping("/{id}")
    public ApiResponse<ComplaintDetailVO> getComplaintDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        ComplaintDetailVO detail = complaintService.getComplaintDetail(id, userId);
        return ApiResponse.success(detail);
    }
}