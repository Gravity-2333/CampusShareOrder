package com.campusshareorder.backend.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.order.MyOrderQueryRequest;
import com.campusshareorder.backend.dto.user.UpdateProfileRequest;
import com.campusshareorder.backend.dto.user.UserCreditQueryRequest;
import com.campusshareorder.backend.dto.user.VerifyStudentRequest;
import com.campusshareorder.backend.entity.CreditChangeRecord;
import com.campusshareorder.backend.entity.CapitalRecord;
import com.campusshareorder.backend.entity.AdminAccount;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.CapitalRecordMapper;
import com.campusshareorder.backend.mapper.AdminAccountMapper;
import com.campusshareorder.backend.mapper.CreditChangeRecordMapper;
import com.campusshareorder.backend.mapper.GroupOrderMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.utils.SecurityUtils;
import com.campusshareorder.backend.vo.common.PageVO;
import com.campusshareorder.backend.vo.order.MyOrderListItemVO;
import com.campusshareorder.backend.vo.user.UserCreditItemVO;
import com.campusshareorder.backend.vo.user.UserCreditVO;
import com.campusshareorder.backend.vo.user.UserCapitalRecordVO;
import com.campusshareorder.backend.vo.user.UserProfileVO;
import com.campusshareorder.backend.vo.user.VerifyStudentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final OrderService orderService;
    private final UserAccountMapper userAccountMapper;
    private final CreditChangeRecordMapper creditChangeRecordMapper;
    private final CapitalRecordMapper capitalRecordMapper;
    private final GroupOrderMapper groupOrderMapper;
    private final AdminAccountMapper adminAccountMapper;

    @GetMapping("/my-orders")
    public ApiResponse<PageVO<MyOrderListItemVO>> getMyOrders(MyOrderQueryRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        return ApiResponse.success(orderService.getMyOrders(request, userId));
    }

    @PostMapping("/verify-student")
    public ApiResponse<VerifyStudentVO> verifyStudent(@Valid @RequestBody VerifyStudentRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        UserAccount user = requireUser(userId);
        String studentNo = request.getStudentNo().trim();

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "已完成实名认证，不能重复认证");
        }

        Long duplicatedCount = userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getStudentNo, studentNo)
                .ne(UserAccount::getId, userId));
        if (duplicatedCount > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "该学号已被认证");
        }

        user.setStudentNo(studentNo);
        user.setIsVerified(true);
        userAccountMapper.updateById(user);

        VerifyStudentVO vo = new VerifyStudentVO();
        vo.setIsVerified(true);
        vo.setStudentNo(user.getStudentNo());
        return ApiResponse.success(vo);
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> getProfile() {
        UserAccount user = requireUser(SecurityUtils.getRequiredUserId());

        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setStudentNo(user.getStudentNo());
        vo.setIsVerified(user.getIsVerified());
        vo.setCreditScore(user.getCreditScore());
        vo.setStatus(user.getStatus());
        vo.setContactInfo(user.getContactInfo());
        vo.setCreatedAt(user.getCreatedAt());
        return ApiResponse.success(vo);
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserAccount user = requireUser(SecurityUtils.getRequiredUserId());

        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getContactInfo() != null) {
            user.setContactInfo(request.getContactInfo());
        }
        userAccountMapper.updateById(user);

        return ApiResponse.success();
    }

    @GetMapping("/credit")
    public ApiResponse<UserCreditVO> getCreditRecords(UserCreditQueryRequest request) {
        UserAccount user = requireUser(SecurityUtils.getRequiredUserId());

        LambdaQueryWrapper<CreditChangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditChangeRecord::getUserId, user.getId())
                .orderByDesc(CreditChangeRecord::getCreatedAt);
        Page<CreditChangeRecord> page = creditChangeRecordMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()),
                wrapper
        );

        Map<Long, Integer> scoreAfterRecordMap = buildScoreAfterRecordMap(user);
        var items = page.getRecords().stream().map(record -> {
            UserCreditItemVO vo = new UserCreditItemVO();
            vo.setRecordId(record.getId());
            vo.setDelta(record.getChangeValue());
            vo.setCurrentScore(scoreAfterRecordMap.getOrDefault(record.getId(), user.getCreditScore()));
            vo.setChangeReason(record.getRemark());
            vo.setReasonType(record.getReasonType());
            vo.setRemark(record.getRemark());
            vo.setRelatedOrderId(record.getRelatedOrderId());
            vo.setRelatedComplaintId(record.getRelatedComplaintId());
            vo.setCreatedAt(record.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());

        UserCreditVO creditVO = new UserCreditVO();
        creditVO.setCreditScore(user.getCreditScore());
        creditVO.setRecords(items);
        creditVO.setPage(page.getCurrent());
        creditVO.setPageSize(page.getSize());
        creditVO.setTotal(page.getTotal());
        creditVO.setPages(page.getPages());
        return ApiResponse.success(creditVO);
    }

    private Map<Long, Integer> buildScoreAfterRecordMap(UserAccount user) {
        LambdaQueryWrapper<CreditChangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditChangeRecord::getUserId, user.getId())
                .orderByDesc(CreditChangeRecord::getCreatedAt);
        List<CreditChangeRecord> records = creditChangeRecordMapper.selectList(wrapper);

        Map<Long, Integer> scoreAfterRecordMap = new HashMap<>();
        int runningScore = user.getCreditScore() == null ? 0 : user.getCreditScore();
        for (CreditChangeRecord record : records) {
            if (record.getId() != null) {
                scoreAfterRecordMap.put(record.getId(), runningScore);
            }
            runningScore -= record.getChangeValue() == null ? 0 : record.getChangeValue();
        }
        return scoreAfterRecordMap;
    }

    @GetMapping("/capital-records")
    public ApiResponse<PageVO<UserCapitalRecordVO>> getCapitalRecords(
            Integer page,
            Integer pageSize,
            String type
    ) {
        Long userId = SecurityUtils.getRequiredUserId();
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);

        LambdaQueryWrapper<CapitalRecord> wrapper = new LambdaQueryWrapper<CapitalRecord>()
                .eq(CapitalRecord::getUserId, userId);
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(CapitalRecord::getType, type.trim());
        }
        wrapper.orderByDesc(CapitalRecord::getCreatedAt);

        Page<CapitalRecord> recordPage = capitalRecordMapper.selectPage(
                new Page<>(normalizedPage, normalizedPageSize),
                wrapper
        );
        UserAccount user = requireUser(userId);
        var records = recordPage.getRecords().stream().map(record -> {
            UserCapitalRecordVO vo = new UserCapitalRecordVO();
            vo.setBizNo(record.getBizNo());
            vo.setOrderId(record.getGroupOrderId());
            vo.setType(record.getType());
            vo.setAmount(record.getAmount());
            vo.setStatus(record.getStatus());
            vo.setRemark(record.getRemark());
            vo.setCreatedAt(record.getCreatedAt());
            vo.setOperatorName(resolveCapitalOperator(record, user));
            GroupOrder order = record.getGroupOrderId() == null
                    ? null
                    : groupOrderMapper.selectById(record.getGroupOrderId());
            vo.setOrderNo(order == null ? "" : order.getOrderNo());
            vo.setReceiverName(resolveCapitalReceiver(record, user, order));
            return vo;
        }).collect(Collectors.toList());

        return ApiResponse.success(new PageVO<>(
                records,
                recordPage.getTotal(),
                recordPage.getCurrent(),
                recordPage.getSize(),
                recordPage.getPages()
        ));
    }

    private UserAccount requireUser(Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return user;
    }

    private String resolveCapitalOperator(CapitalRecord record, UserAccount user) {
        if ("USER".equals(record.getOperatorType()) && record.getOperatorId() != null) {
            UserAccount operator = userAccountMapper.selectById(record.getOperatorId());
            return operator == null ? "用户" : operator.getNickname();
        }
        if ("ADMIN".equals(record.getOperatorType()) && record.getOperatorId() != null) {
            AdminAccount operator = adminAccountMapper.selectById(record.getOperatorId());
            return operator == null ? "管理员" : operator.getUsername();
        }
        if (record.getOperatorType() == null
                && ("PAY".equals(record.getType()) || "REFUND_EXIT".equals(record.getType()))) {
            return user.getNickname();
        }
        return "系统";
    }

    private String resolveCapitalReceiver(CapitalRecord record, UserAccount user, GroupOrder order) {
        if ("PAY".equals(record.getType())) {
            return "平台资金账户";
        }
        if (record.getType() != null && record.getType().startsWith("REFUND")) {
            return user.getNickname();
        }
        if ("SETTLE_TO_CREATOR".equals(record.getType()) && order != null) {
            UserAccount creator = userAccountMapper.selectById(order.getCreatorUserId());
            return creator == null ? "订单发起人" : creator.getNickname();
        }
        return user.getNickname();
    }
}
