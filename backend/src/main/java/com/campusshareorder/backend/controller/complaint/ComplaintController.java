package com.campusshareorder.backend.controller.complaint;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.complaint.CreateComplaintRequest;
import com.campusshareorder.backend.dto.complaint.MyComplaintQueryRequest;
import com.campusshareorder.backend.service.ComplaintService;
import com.campusshareorder.backend.utils.SecurityUtils;
import com.campusshareorder.backend.vo.complaint.ComplaintDetailVO;
import com.campusshareorder.backend.vo.complaint.ComplaintListItemVO;
import com.campusshareorder.backend.vo.complaint.CreateComplaintVO;
import com.campusshareorder.backend.vo.common.PageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ApiResponse<CreateComplaintVO> createComplaint(@RequestBody CreateComplaintRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(complaintService.createComplaint(request, userId));
    }

    @GetMapping("/my")
    public ApiResponse<PageVO<ComplaintListItemVO>> getMyComplaints(MyComplaintQueryRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        PageVO<ComplaintListItemVO> page = complaintService.getMyComplaints(request, userId);
        return ApiResponse.success(page);
    }

    @GetMapping("/{id}")
    public ApiResponse<ComplaintDetailVO> getComplaintDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        ComplaintDetailVO detail = complaintService.getComplaintDetail(id, userId);
        return ApiResponse.success(detail);
    }
}
