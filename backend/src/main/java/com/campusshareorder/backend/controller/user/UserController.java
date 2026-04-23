package com.campusshareorder.backend.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.dto.user.UpdateProfileRequest;
import com.campusshareorder.backend.dto.user.VerifyStudentRequest;
import com.campusshareorder.backend.dto.order.MyOrderQueryRequest;
import com.campusshareorder.backend.entity.CreditChangeRecord;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.CreditChangeRecordMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.utils.SecurityUtils;
import com.campusshareorder.backend.vo.common.PageVO;
import com.campusshareorder.backend.vo.order.MyOrderListItemVO;
import com.campusshareorder.backend.vo.user.CreditRecordVO;
import com.campusshareorder.backend.vo.user.UserProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        PageVO<MyOrderListItemVO> page = orderService.getMyOrders(request, userId);
        return ApiResponse.success(page);
    }

    @PostMapping("/verify-student")
    public ApiResponse<Void> verifyStudent(@Valid @RequestBody VerifyStudentRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStudentNo(request.getStudentNo());
        user.setIsVerified(true);
        userAccountMapper.updateById(user);

        return ApiResponse.success();
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> getProfile() {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

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
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

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
    public ApiResponse<List<CreditRecordVO>> getCreditRecords() {
        Long userId = SecurityUtils.getRequiredCurrentUserId();

        LambdaQueryWrapper<CreditChangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditChangeRecord::getUserId, userId)
                .orderByDesc(CreditChangeRecord::getCreatedAt);
        List<CreditChangeRecord> records = creditChangeRecordMapper.selectList(wrapper);

        List<CreditRecordVO> voList = records.stream().map(record -> {
            CreditRecordVO vo = new CreditRecordVO();
            vo.setRecordId(record.getId());
            vo.setChangeValue(record.getChangeValue());
            vo.setReasonType(record.getReasonType());
            vo.setRemark(record.getRemark());
            vo.setRelatedOrderId(record.getRelatedOrderId());
            vo.setRelatedComplaintId(record.getRelatedComplaintId());
            vo.setCreatedAt(record.getCreatedAt());

            UserAccount user = userAccountMapper.selectById(userId);
            if (user != null) {
                vo.setCurrentScore(user.getCreditScore());
            }
            return vo;
        }).collect(Collectors.toList());

        return ApiResponse.success(voList);
    }
}