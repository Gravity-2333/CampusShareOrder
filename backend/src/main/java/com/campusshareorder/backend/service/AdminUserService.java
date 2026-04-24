package com.campusshareorder.backend.service;

import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.dto.admin.AdminUserQueryRequest;
import com.campusshareorder.backend.vo.admin.AdminUserItemVO;

public interface AdminUserService {
    PageResult<AdminUserItemVO> listUsers(AdminUserQueryRequest request);
    
    void banUser(Long userId, Long adminId);
    
    void unbanUser(Long userId, Long adminId);
}
