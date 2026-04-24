package com.campusshareorder.backend.service;

import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.dto.admin.AdminHandleComplaintRequest;
import com.campusshareorder.backend.dto.admin.AdminComplaintQueryRequest;
import com.campusshareorder.backend.vo.complaint.ComplaintDetailVO;
import com.campusshareorder.backend.vo.admin.AdminComplaintItemVO;

public interface AdminComplaintService {
    PageResult<AdminComplaintItemVO> listComplaints(AdminComplaintQueryRequest request);
    
    ComplaintDetailVO getComplaintDetail(Long complaintId);
    
    void handleComplaint(Long complaintId, AdminHandleComplaintRequest request, Long adminId);
}
