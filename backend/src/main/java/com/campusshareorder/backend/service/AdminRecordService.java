package com.campusshareorder.backend.service;

import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.dto.admin.CapitalRecordQueryRequest;
import com.campusshareorder.backend.dto.admin.OperationLogQueryRequest;
import com.campusshareorder.backend.vo.admin.CapitalRecordItemVO;
import com.campusshareorder.backend.vo.admin.OperationLogItemVO;

public interface AdminRecordService {
    PageResult<CapitalRecordItemVO> listCapitalRecords(CapitalRecordQueryRequest request);
    
    PageResult<OperationLogItemVO> listOperationLogs(OperationLogQueryRequest request);
}
