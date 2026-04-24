package com.campusshareorder.backend.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.order.MyOrderQueryRequest;
import com.campusshareorder.backend.dto.user.UpdateProfileRequest;
import com.campusshareorder.backend.dto.user.VerifyStudentRequest;
import com.campusshareorder.backend.entity.CreditChangeRecord;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.CreditChangeRecordMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.utils.SecurityUtils;
import com.campusshareorder.backend.vo.common.PageVO;
import com.campusshareorder.backend.vo.order.MyOrderListItemVO;
import com.campusshareorder.backend.vo.user.UserCreditItemVO;
import com.campusshareorder.backend.vo.user.UserCreditVO;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final OrderService orderService;
    private final UserAccountMapper userAccountMapper;
    private final CreditChangeRecordMapper creditChangeRecordMapper;

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
    public ApiResponse<UserCreditVO> getCreditRecords() {
        UserAccount user = requireUser(SecurityUtils.getRequiredUserId());

        LambdaQueryWrapper<CreditChangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditChangeRecord::getUserId, user.getId())
                .orderByDesc(CreditChangeRecord::getCreatedAt);
        List<CreditChangeRecord> records = creditChangeRecordMapper.selectList(wrapper);

        List<UserCreditItemVO> items = records.stream().map(record -> {
            UserCreditItemVO vo = new UserCreditItemVO();
            vo.setRecordId(record.getId());
            vo.setDelta(record.getChangeValue());
            vo.setCurrentScore(user.getCreditScore());
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
        return ApiResponse.success(creditVO);
    }

    private UserAccount requireUser(Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return user;
    }
}
