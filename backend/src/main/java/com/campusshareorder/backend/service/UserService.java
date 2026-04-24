package com.campusshareorder.backend.service;

import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.dto.order.MyOrderQueryRequest;
import com.campusshareorder.backend.dto.user.UpdateProfileRequest;
import com.campusshareorder.backend.dto.user.VerifyStudentRequest;
import com.campusshareorder.backend.vo.order.MyOrderListItemVO;
import com.campusshareorder.backend.vo.user.UserCreditVO;
import com.campusshareorder.backend.vo.user.UserProfileVO;
import com.campusshareorder.backend.vo.user.VerifyStudentVO;

public interface UserService {
    VerifyStudentVO verifyStudent(Long userId, VerifyStudentRequest request);

    UserProfileVO getProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);

    UserCreditVO getCreditRecords(Long userId);

    PageResult<MyOrderListItemVO> getMyOrders(MyOrderQueryRequest request, Long userId);
}
