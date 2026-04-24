package com.campusshareorder.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusshareorder.backend.common.response.PageResult;
import com.campusshareorder.backend.dto.admin.CapitalRecordQueryRequest;
import com.campusshareorder.backend.dto.admin.OperationLogQueryRequest;
import com.campusshareorder.backend.entity.CapitalRecord;
import com.campusshareorder.backend.entity.OperationLog;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.CapitalRecordMapper;
import com.campusshareorder.backend.mapper.OperationLogMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.AdminRecordService;
import com.campusshareorder.backend.vo.admin.CapitalRecordItemVO;
import com.campusshareorder.backend.vo.admin.OperationLogItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRecordServiceImpl implements AdminRecordService {

    private final CapitalRecordMapper capitalRecordMapper;
    private final OperationLogMapper operationLogMapper;
    private final UserAccountMapper userAccountMapper;

    @Override
    public PageResult<CapitalRecordItemVO> listCapitalRecords(CapitalRecordQueryRequest request) {
        Page<CapitalRecord> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<CapitalRecord> wrapper = new LambdaQueryWrapper<>();

        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            wrapper.eq(CapitalRecord::getType, request.getType());
        }
        if (request.getUserId() != null) {
            wrapper.eq(CapitalRecord::getUserId, request.getUserId());
        }
        if (request.getOrderId() != null) {
            wrapper.eq(CapitalRecord::getGroupOrderId, request.getOrderId());
        }
        wrapper.orderByDesc(CapitalRecord::getCreatedAt);

        Page<CapitalRecord> recordPage = capitalRecordMapper.selectPage(page, wrapper);
        List<CapitalRecordItemVO> list = recordPage.getRecords().stream().map(record -> {
            CapitalRecordItemVO vo = new CapitalRecordItemVO();
            vo.setId(record.getId());
            vo.setBizNo(record.getBizNo());
            vo.setUserId(record.getUserId());
            vo.setGroupOrderId(record.getGroupOrderId());
            vo.setMemberId(record.getMemberId());
            vo.setType(record.getType());
            vo.setAmount(record.getAmount());
            vo.setStatus(record.getStatus());
            vo.setRemark(record.getRemark());
            vo.setCreatedAt(record.getCreatedAt());

            UserAccount user = userAccountMapper.selectById(record.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(list, request.getPage(), request.getPageSize(), recordPage.getTotal());
    }

    @Override
    public PageResult<OperationLogItemVO> listOperationLogs(OperationLogQueryRequest request) {
        Page<OperationLog> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (request.getBizType() != null && !request.getBizType().trim().isEmpty()) {
            wrapper.eq(OperationLog::getBizType, request.getBizType());
        }
        if (request.getBizId() != null) {
            wrapper.eq(OperationLog::getBizId, request.getBizId());
        }
        wrapper.orderByDesc(OperationLog::getCreatedAt);

        Page<OperationLog> logPage = operationLogMapper.selectPage(page, wrapper);
        List<OperationLogItemVO> list = logPage.getRecords().stream().map(log -> {
            OperationLogItemVO vo = new OperationLogItemVO();
            vo.setId(log.getId());
            vo.setOperatorType(log.getOperatorType());
            vo.setOperatorId(log.getOperatorId());
            vo.setBizType(log.getBizType());
            vo.setBizId(log.getBizId());
            vo.setAction(log.getAction());
            vo.setDetailJson(log.getDetailJson());
            vo.setCreatedAt(log.getCreatedAt());

            if (log.getOperatorId() != null && "USER".equals(log.getOperatorType())) {
                UserAccount user = userAccountMapper.selectById(log.getOperatorId());
                if (user != null) {
                    vo.setOperatorName(user.getNickname());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(list, request.getPage(), request.getPageSize(), logPage.getTotal());
    }
}
