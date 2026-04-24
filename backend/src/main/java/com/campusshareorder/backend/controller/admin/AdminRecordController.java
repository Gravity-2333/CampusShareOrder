package com.campusshareorder.backend.controller.admin;

import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.common.util.SecurityUtils;
import com.campusshareorder.backend.dto.admin.CapitalRecordQueryRequest;
import com.campusshareorder.backend.dto.admin.OperationLogQueryRequest;
import com.campusshareorder.backend.service.AdminRecordService;
import com.campusshareorder.backend.vo.admin.CapitalRecordItemVO;
import com.campusshareorder.backend.vo.admin.OperationLogItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/records")
@RequiredArgsConstructor
public class AdminRecordController {

    private final AdminRecordService adminRecordService;

    @GetMapping("/capital")
    public ApiResponse<PageResult<CapitalRecordItemVO>> getCapitalRecords(CapitalRecordQueryRequest request) {
        return ApiResponse.success(adminRecordService.listCapitalRecords(request));
    }

    @GetMapping("/logs")
    public ApiResponse<PageResult<OperationLogItemVO>> getOperationLogs(OperationLogQueryRequest request) {
        return ApiResponse.success(adminRecordService.listOperationLogs(request));
    }
}
