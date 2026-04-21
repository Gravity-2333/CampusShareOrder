package com.campusshareorder.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusshareorder.backend.dto.complaint.CreateComplaintRequest;
import com.campusshareorder.backend.entity.Complaint;
import com.campusshareorder.backend.vo.complaint.ComplaintDetailVO;
import com.campusshareorder.backend.vo.complaint.ComplaintListItemVO;

import java.util.List;

public interface ComplaintService extends IService<Complaint> {
    void createComplaint(CreateComplaintRequest request, Long userId);
    List<ComplaintListItemVO> getMyComplaints(Long userId);
    ComplaintDetailVO getComplaintDetail(Long id, Long userId);
}