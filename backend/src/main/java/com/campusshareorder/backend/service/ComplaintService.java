package com.campusshareorder.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.dto.complaint.CreateComplaintRequest;
import com.campusshareorder.backend.dto.complaint.MyComplaintQueryRequest;
import com.campusshareorder.backend.entity.Complaint;
import com.campusshareorder.backend.vo.complaint.ComplaintDetailVO;
import com.campusshareorder.backend.vo.complaint.ComplaintListItemVO;
import com.campusshareorder.backend.vo.complaint.CreateComplaintVO;

public interface ComplaintService extends IService<Complaint> {
    CreateComplaintVO createComplaint(CreateComplaintRequest request, Long userId);

    PageResult<ComplaintListItemVO> getMyComplaints(MyComplaintQueryRequest request, Long userId);

    ComplaintDetailVO getComplaintDetail(Long id, Long userId);
}
