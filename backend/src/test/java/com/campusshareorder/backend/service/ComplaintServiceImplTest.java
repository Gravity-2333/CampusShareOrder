package com.campusshareorder.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.dto.complaint.CreateComplaintRequest;
import com.campusshareorder.backend.entity.Complaint;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.mapper.ComplaintMapper;
import com.campusshareorder.backend.mapper.GroupOrderMapper;
import com.campusshareorder.backend.mapper.GroupOrderMemberMapper;
import com.campusshareorder.backend.mapper.OperationLogMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.impl.ComplaintServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

    @Mock
    private ComplaintMapper complaintMapper;
    @Mock
    private GroupOrderMapper groupOrderMapper;
    @Mock
    private GroupOrderMemberMapper groupOrderMemberMapper;
    @Mock
    private OperationLogMapper operationLogMapper;
    @Mock
    private UserAccountMapper userAccountMapper;

    @InjectMocks
    private ComplaintServiceImpl complaintService;

    @Test
    void createComplaintRejectsDuplicateComplaint() {
        GroupOrder order = new GroupOrder();
        order.setId(1L);
        order.setCreatorUserId(100L);
        order.setComplaintOpened(true);
        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(1L)
                .thenReturn(1L);
        when(complaintMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        CreateComplaintRequest request = new CreateComplaintRequest();
        request.setOrderId(1L);
        request.setType("NOT_PURCHASED");
        request.setContent("发起人未按时上传凭证");

        assertThatThrownBy(() -> complaintService.createComplaint(request, 101L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.COMPLAINT_DUPLICATED.getCode()));

        verify(complaintMapper, never()).insert(any(Complaint.class));
    }

    @Test
    void createComplaintRejectsInactiveComplainant() {
        GroupOrder order = new GroupOrder();
        order.setId(1L);
        order.setCreatorUserId(100L);
        order.setComplaintOpened(true);
        when(groupOrderMapper.selectById(1L)).thenReturn(order);
        when(groupOrderMemberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        CreateComplaintRequest request = new CreateComplaintRequest();
        request.setOrderId(1L);
        request.setType("NOT_PURCHASED");
        request.setContent("发起人未按时上传凭证");

        assertThatThrownBy(() -> complaintService.createComplaint(request, 101L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.FORBIDDEN.getCode()));

        verify(complaintMapper, never()).insert(any(Complaint.class));
    }
}
