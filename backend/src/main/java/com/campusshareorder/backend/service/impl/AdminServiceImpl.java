package com.campusshareorder.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.dto.admin.BanUserRequest;
import com.campusshareorder.backend.dto.admin.CancelOrderRequest;
import com.campusshareorder.backend.dto.admin.HandleComplaintRequest;
import com.campusshareorder.backend.entity.AdminAccount;
import com.campusshareorder.backend.entity.CapitalRecord;
import com.campusshareorder.backend.entity.Complaint;
import com.campusshareorder.backend.entity.CreditChangeRecord;
import com.campusshareorder.backend.entity.GroupOrder;
import com.campusshareorder.backend.entity.GroupOrderMember;
import com.campusshareorder.backend.entity.Notification;
import com.campusshareorder.backend.entity.OperationLog;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.AdminAccountMapper;
import com.campusshareorder.backend.mapper.CapitalRecordMapper;
import com.campusshareorder.backend.mapper.ComplaintMapper;
import com.campusshareorder.backend.mapper.CreditChangeRecordMapper;
import com.campusshareorder.backend.mapper.GroupOrderMapper;
import com.campusshareorder.backend.mapper.GroupOrderMemberMapper;
import com.campusshareorder.backend.mapper.NotificationMapper;
import com.campusshareorder.backend.mapper.OperationLogMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.AdminService;
import com.campusshareorder.backend.service.OrderService;
import com.campusshareorder.backend.vo.admin.AdminCapitalRecordVO;
import com.campusshareorder.backend.vo.admin.AdminComplaintDetailVO;
import com.campusshareorder.backend.vo.admin.AdminDashboardMetricsVO;
import com.campusshareorder.backend.vo.admin.AdminDashboardOverviewVO;
import com.campusshareorder.backend.vo.admin.AdminCreditRecordVO;
import com.campusshareorder.backend.vo.admin.AdminOperationLogVO;
import com.campusshareorder.backend.vo.admin.AdminUserDetailVO;
import com.campusshareorder.backend.vo.admin.AdminUserListItemVO;
import com.campusshareorder.backend.vo.common.PageVO;
import com.campusshareorder.backend.vo.complaint.ComplaintListItemVO;
import com.campusshareorder.backend.vo.order.ComplaintInfoVO;
import com.campusshareorder.backend.vo.order.OrderDetailVO;
import com.campusshareorder.backend.vo.order.OrderListItemVO;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminAccountMapper adminAccountMapper;
    private final CapitalRecordMapper capitalRecordMapper;
    private final ComplaintMapper complaintMapper;
    private final CreditChangeRecordMapper creditChangeRecordMapper;
    private final GroupOrderMapper groupOrderMapper;
    private final GroupOrderMemberMapper groupOrderMemberMapper;
    private final NotificationMapper notificationMapper;
    private final OperationLogMapper operationLogMapper;
    private final OrderService orderService;
    private final UserAccountMapper userAccountMapper;

    @Override
    public AdminDashboardOverviewVO getDashboardOverview() {
        AdminDashboardMetricsVO metrics = new AdminDashboardMetricsVO();
        metrics.setUsers(userAccountMapper.selectCount(null));
        metrics.setOrders(groupOrderMapper.selectCount(null));
        metrics.setComplaints(complaintMapper.selectCount(null));

        AdminDashboardOverviewVO overview = new AdminDashboardOverviewVO();
        overview.setMetrics(metrics);
        overview.setRecentOrders(getOrders("", 1, 5).getList());
        overview.setRecentComplaints(getComplaints(1, 5).getList());
        overview.setRecentLogs(getOperationLogs("", 1, 5).getList());
        return overview;
    }

    @Override
    public PageVO<AdminUserListItemVO> getUsers(String keyword, String status, Integer page, Integer pageSize) {
        Page<UserAccount> pageRequest = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(UserAccount::getNickname, keyword)
                    .or().like(UserAccount::getPhone, keyword));
        }

        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(UserAccount::getStatus, status);
        }

        wrapper.orderByDesc(UserAccount::getCreatedAt);
        Page<UserAccount> userPage = userAccountMapper.selectPage(pageRequest, wrapper);

        List<AdminUserListItemVO> list = userPage.getRecords().stream().map(this::toAdminUserListItem).collect(Collectors.toList());
        return new PageVO<>(list, userPage.getTotal(), userPage.getCurrent(), userPage.getSize(), userPage.getPages());
    }

    @Override
    public AdminUserDetailVO getUserDetail(Long userId) {
        UserAccount user = requireUser(userId);

        AdminUserDetailVO detail = new AdminUserDetailVO();
        detail.setUserId(user.getId());
        detail.setPhone(user.getPhone());
        detail.setNickname(user.getNickname());
        detail.setStudentNo(user.getStudentNo());
        detail.setIsVerified(user.getIsVerified());
        detail.setCreditScore(user.getCreditScore());
        detail.setStatus(user.getStatus());
        detail.setContactInfo(user.getContactInfo());
        detail.setCreatedAt(user.getCreatedAt());

        LambdaQueryWrapper<CreditChangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditChangeRecord::getUserId, userId).orderByDesc(CreditChangeRecord::getCreatedAt);
        List<AdminCreditRecordVO> records = creditChangeRecordMapper.selectList(wrapper).stream().map(record -> {
            AdminCreditRecordVO vo = new AdminCreditRecordVO();
            vo.setCreatedAt(record.getCreatedAt());
            vo.setChangeReason(record.getRemark() == null || record.getRemark().isBlank() ? record.getReasonType() : record.getRemark());
            vo.setDelta(record.getChangeValue());
            return vo;
        }).collect(Collectors.toList());
        detail.setCreditRecords(records);

        return detail;
    }

    @Override
    @Transactional
    public void banUser(Long userId, BanUserRequest request, Long adminId) {
        UserAccount user = requireUser(userId);
        user.setStatus("BANNED");
        userAccountMapper.updateById(user);
        insertOperationLog("ADMIN", adminId, "USER", userId, "USER_BANNED",
                request == null ? null : request.getReason());
    }

    @Override
    @Transactional
    public void unbanUser(Long userId, Long adminId) {
        UserAccount user = requireUser(userId);
        user.setStatus("NORMAL");
        userAccountMapper.updateById(user);
        insertOperationLog("ADMIN", adminId, "USER", userId, "USER_UNBANNED", null);
    }

    @Override
    public PageVO<OrderListItemVO> getOrders(String status, Integer page, Integer pageSize) {
        Page<GroupOrder> pageRequest = new Page<>(page, pageSize);
        LambdaQueryWrapper<GroupOrder> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(GroupOrder::getStatus, denormalizeOrderStatus(status));
        }

        wrapper.orderByDesc(GroupOrder::getCreatedAt);
        Page<GroupOrder> orderPage = groupOrderMapper.selectPage(pageRequest, wrapper);

        List<OrderListItemVO> list = orderPage.getRecords().stream().map(order -> {
            OrderListItemVO vo = new OrderListItemVO();
            vo.setOrderId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setProductName(order.getProductName());
            vo.setProductDesc(order.getProductDesc());
            vo.setTotalMemberCount(order.getTotalMemberCount());
            vo.setCurrentMemberCount(order.getCurrentMemberCount());
            vo.setRemainingCount(order.getTotalMemberCount() - order.getCurrentMemberCount());
            vo.setEstimatedTotalAmount(order.getEstimatedTotalAmount());
            vo.setEstimatedPerAmount(order.getEstimatedPerAmount());
            vo.setPickupPoint(order.getPickupPoint());
            vo.setStatus(normalizeOrderStatus(order.getStatus()));
            vo.setDeadlineAt(order.getDeadlineAt());
            vo.setCreatedAt(order.getCreatedAt());

            UserAccount creator = userAccountMapper.selectById(order.getCreatorUserId());
            if (creator != null) {
                vo.setCreatorNickname(creator.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageVO<>(list, orderPage.getTotal(), orderPage.getCurrent(), orderPage.getSize(), orderPage.getPages());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        OrderDetailVO detail = orderService.getOrderDetail(orderId, -1L);
        detail.setViewerRoleInOrder("ADMIN");
        if (detail.getBasicInfo() != null) {
            detail.getBasicInfo().setStatus(normalizeOrderStatus(detail.getBasicInfo().getStatus()));
        }
        if (detail.getComplaintInfo() == null) {
            ComplaintInfoVO complaintInfo = new ComplaintInfoVO();
            complaintInfo.setComplaintOpened(false);
            detail.setComplaintInfo(complaintInfo);
        }
        return detail;
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, CancelOrderRequest request, Long adminId) {
        GroupOrder order = requireOrder(orderId);
        if ("COMPLETED".equals(order.getStatus()) || "CANCELED".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "终态订单不能取消");
        }

        order.setStatus("CANCELED");
        order.setCancelReason(request == null ? null : request.getReason());
        groupOrderMapper.updateById(order);
        refundActiveMembers(orderId, "管理员取消订单退款");
        insertOperationLog("ADMIN", adminId, "ORDER", orderId, "ORDER_CANCELED_BY_ADMIN",
                request == null ? null : request.getReason());
        notifyOrderMembers(orderId, "ORDER_CANCELED", "订单已被管理员取消",
                "管理员已取消该订单，系统会按规则处理退款。");
    }

    @Override
    public PageVO<ComplaintListItemVO> getComplaints(Integer page, Integer pageSize) {
        Page<Complaint> pageRequest = new Page<>(page, pageSize);
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Complaint::getCreatedAt);
        Page<Complaint> complaintPage = complaintMapper.selectPage(pageRequest, wrapper);

        List<ComplaintListItemVO> list = complaintPage.getRecords().stream().map(this::toComplaintListItem).collect(Collectors.toList());
        return new PageVO<>(list, complaintPage.getTotal(), complaintPage.getCurrent(), complaintPage.getSize(), complaintPage.getPages());
    }

    @Override
    public AdminComplaintDetailVO getComplaintDetail(Long complaintId) {
        Complaint complaint = requireComplaint(complaintId);
        GroupOrder order = groupOrderMapper.selectById(complaint.getGroupOrderId());
        UserAccount accused = complaint.getAccusedUserId() == null ? null : userAccountMapper.selectById(complaint.getAccusedUserId());

        AdminComplaintDetailVO detail = new AdminComplaintDetailVO();
        detail.setComplaintId(complaint.getId());
        detail.setComplaintNo(complaint.getComplaintNo());
        detail.setOrderId(complaint.getGroupOrderId());
        detail.setOrderNo(order == null ? "" : order.getOrderNo());
        detail.setProductName(order == null ? "" : order.getProductName());
        detail.setComplainantUserId(complaint.getComplainantUserId());
        detail.setAccusedNickname(accused == null ? "" : accused.getNickname());
        detail.setType(complaint.getType());
        detail.setContent(complaint.getContent());
        detail.setStatus(complaint.getStatus());
        detail.setHandleResult(complaint.getHandleResult());
        detail.setHandledAt(complaint.getHandledAt());
        detail.setCreatedAt(complaint.getCreatedAt());
        detail.setOpenedBySystem(order != null && Boolean.TRUE.equals(order.getComplaintOpened()));
        return detail;
    }

    @Override
    @Transactional
    public void handleComplaint(Long complaintId, HandleComplaintRequest request, Long adminId) {
        Complaint complaint = requireComplaint(complaintId);
        if ("PROCESSED".equals(complaint.getStatus())) {
            throw new BusinessException(ErrorCode.COMPLAINT_ALREADY_PROCESSED);
        }

        complaint.setStatus("PROCESSED");
        complaint.setHandleResult(request == null ? null : request.getHandleResult());
        complaint.setHandledAt(LocalDateTime.now());
        complaint.setHandledByAdminId(adminId);
        complaintMapper.updateById(complaint);

        GroupOrder order = groupOrderMapper.selectById(complaint.getGroupOrderId());
        if (order != null && !"COMPLETED".equals(order.getStatus()) && !"CANCELED".equals(order.getStatus())) {
            order.setStatus("CANCELED");
            order.setComplaintOpened(true);
            groupOrderMapper.updateById(order);
            refundActiveMembers(order.getId(), "投诉处理取消订单退款");
        }

        applyComplaintCreditPenalty(complaint);
        insertOperationLog("ADMIN", adminId, "COMPLAINT", complaintId, "COMPLAINT_HANDLED",
                request == null ? null : request.getHandleResult());
        insertNotification(complaint.getComplainantUserId(), "COMPLAINT_HANDLED", "投诉已处理",
                "你的投诉已由管理员处理，请查看处理结果。", complaint.getGroupOrderId(), complaintId);
        insertNotification(complaint.getAccusedUserId(), "COMPLAINT_HANDLED", "投诉处理完成",
                "与你相关的投诉已处理，请关注订单和信用分变化。", complaint.getGroupOrderId(), complaintId);
    }

    @Override
    public PageVO<AdminCapitalRecordVO> getCapitalRecords(String type, Integer page, Integer pageSize) {
        Page<CapitalRecord> pageRequest = new Page<>(page, pageSize);
        LambdaQueryWrapper<CapitalRecord> wrapper = new LambdaQueryWrapper<>();

        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(CapitalRecord::getType, type);
        }

        wrapper.orderByDesc(CapitalRecord::getCreatedAt);
        Page<CapitalRecord> capitalPage = capitalRecordMapper.selectPage(pageRequest, wrapper);

        List<AdminCapitalRecordVO> list = capitalPage.getRecords().stream().map(record -> {
            AdminCapitalRecordVO vo = new AdminCapitalRecordVO();
            vo.setBizNo(record.getBizNo());
            vo.setType(record.getType());
            vo.setAmount(record.getAmount());
            vo.setCreatedAt(record.getCreatedAt());
            UserAccount user = record.getUserId() == null ? null : userAccountMapper.selectById(record.getUserId());
            vo.setUserNickname(user == null ? "" : user.getNickname());
            return vo;
        }).collect(Collectors.toList());

        return new PageVO<>(list, capitalPage.getTotal(), capitalPage.getCurrent(), capitalPage.getSize(), capitalPage.getPages());
    }

    @Override
    public PageVO<AdminOperationLogVO> getOperationLogs(String action, Integer page, Integer pageSize) {
        Page<OperationLog> pageRequest = new Page<>(page, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (action != null && !action.trim().isEmpty()) {
            wrapper.like(OperationLog::getAction, action);
        }

        wrapper.orderByDesc(OperationLog::getCreatedAt);
        Page<OperationLog> logPage = operationLogMapper.selectPage(pageRequest, wrapper);

        List<AdminOperationLogVO> list = logPage.getRecords().stream().map(log -> {
            AdminOperationLogVO vo = new AdminOperationLogVO();
            vo.setAction(log.getAction());
            vo.setCreatedAt(log.getCreatedAt());
            vo.setTargetNo(buildTargetNo(log));
            vo.setOperatorName(resolveOperatorName(log));
            return vo;
        }).collect(Collectors.toList());

        return new PageVO<>(list, logPage.getTotal(), logPage.getCurrent(), logPage.getSize(), logPage.getPages());
    }

    private AdminUserListItemVO toAdminUserListItem(UserAccount user) {
        AdminUserListItemVO vo = new AdminUserListItemVO();
        vo.setUserId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setIsVerified(user.getIsVerified());
        vo.setCreditScore(user.getCreditScore());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    private ComplaintListItemVO toComplaintListItem(Complaint complaint) {
        ComplaintListItemVO vo = new ComplaintListItemVO();
        vo.setComplaintId(complaint.getId());
        vo.setComplaintNo(complaint.getComplaintNo());
        vo.setOrderId(complaint.getGroupOrderId());
        vo.setAccusedUserId(complaint.getAccusedUserId());
        vo.setType(complaint.getType());
        vo.setContent(complaint.getContent());
        vo.setStatus(complaint.getStatus());
        vo.setHandleResult(complaint.getHandleResult());
        vo.setCreatedAt(complaint.getCreatedAt());
        vo.setHandledAt(complaint.getHandledAt());

        GroupOrder order = groupOrderMapper.selectById(complaint.getGroupOrderId());
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());
            vo.setProductName(order.getProductName());
            vo.setOpenedBySystem(Boolean.TRUE.equals(order.getComplaintOpened()));
        }

        UserAccount accused = complaint.getAccusedUserId() == null ? null : userAccountMapper.selectById(complaint.getAccusedUserId());
        if (accused != null) {
            vo.setAccusedNickname(accused.getNickname());
        }

        return vo;
    }

    private void refundActiveMembers(Long orderId, String remark) {
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>().eq(GroupOrderMember::getGroupOrderId, orderId)
        );
        for (GroupOrderMember member : members) {
            if ("ACTIVE".equals(member.getJoinStatus()) && "PAID".equals(member.getPayStatus())) {
                BigDecimal paidAmount = member.getPayAmount() == null ? BigDecimal.ZERO : member.getPayAmount();
                BigDecimal refunded = member.getRefundAmountTotal() == null ? BigDecimal.ZERO : member.getRefundAmountTotal();
                BigDecimal netRefund = paidAmount.subtract(refunded).max(BigDecimal.ZERO);
                member.setRefundAmountTotal(refunded.add(netRefund));
                insertCapitalRecord("RFC-M" + member.getId(), member.getUserId(), orderId,
                        member.getId(), "REFUND_CANCEL", netRefund, remark);
            }
            if ("ACTIVE".equals(member.getJoinStatus())) {
                member.setJoinStatus("CANCELED");
            }
            groupOrderMemberMapper.updateById(member);
        }
    }

    private void insertCapitalRecord(String bizNo, Long userId, Long orderId, Long memberId,
                                     String type, BigDecimal amount, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        Long existingCount = capitalRecordMapper.selectCount(
                new LambdaQueryWrapper<CapitalRecord>().eq(CapitalRecord::getBizNo, bizNo)
        );
        if (existingCount != null && existingCount > 0) {
            return;
        }

        CapitalRecord record = new CapitalRecord();
        record.setBizNo(bizNo);
        record.setUserId(userId);
        record.setGroupOrderId(orderId);
        record.setMemberId(memberId);
        record.setType(type);
        record.setAmount(amount);
        record.setStatus("SUCCESS");
        record.setRemark(remark);
        record.setCreatedAt(LocalDateTime.now());
        capitalRecordMapper.insert(record);
    }

    private void applyComplaintCreditPenalty(Complaint complaint) {
        if (complaint.getAccusedUserId() == null) {
            return;
        }

        Long existingCount = creditChangeRecordMapper.selectCount(
                new LambdaQueryWrapper<CreditChangeRecord>()
                        .eq(CreditChangeRecord::getRelatedComplaintId, complaint.getId())
                        .eq(CreditChangeRecord::getReasonType, "COMPLAINT_PENALTY")
        );
        if (existingCount != null && existingCount > 0) {
            return;
        }

        UserAccount accusedUser = userAccountMapper.selectById(complaint.getAccusedUserId());
        if (accusedUser == null) {
            return;
        }

        int delta = -10;
        int currentScore = accusedUser.getCreditScore() == null ? 0 : accusedUser.getCreditScore();
        accusedUser.setCreditScore(Math.max(0, currentScore + delta));
        userAccountMapper.updateById(accusedUser);

        CreditChangeRecord record = new CreditChangeRecord();
        record.setUserId(accusedUser.getId());
        record.setChangeValue(delta);
        record.setReasonType("COMPLAINT_PENALTY");
        record.setRelatedOrderId(complaint.getGroupOrderId());
        record.setRelatedComplaintId(complaint.getId());
        record.setRemark("投诉成立扣减信用分");
        record.setCreatedAt(LocalDateTime.now());
        creditChangeRecordMapper.insert(record);
    }

    private void insertOperationLog(String operatorType, Long operatorId, String bizType, Long bizId, String action, String detail) {
        OperationLog log = new OperationLog();
        log.setOperatorType(operatorType);
        log.setOperatorId(operatorId);
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setAction(action);
        log.setDetailJson(JSONUtil.createObj()
                .set("detail", detail == null ? "" : detail)
                .toString());
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private void notifyOrderMembers(Long orderId, String type, String title, String content) {
        List<GroupOrderMember> members = groupOrderMemberMapper.selectList(
                new LambdaQueryWrapper<GroupOrderMember>().eq(GroupOrderMember::getGroupOrderId, orderId)
        );
        for (GroupOrderMember member : members) {
            insertNotification(member.getUserId(), type, title, content, orderId, null);
        }
    }

    private void insertNotification(Long userId, String type, String title, String content,
                                    Long orderId, Long complaintId) {
        if (userId == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);
        notification.setRelatedOrderId(orderId);
        notification.setRelatedComplaintId(complaintId);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    private String buildTargetNo(OperationLog log) {
        if (log.getBizId() == null || log.getBizType() == null) {
            return "";
        }

        return switch (log.getBizType()) {
            case "USER" -> "USER-" + log.getBizId();
            case "ORDER" -> {
                GroupOrder order = groupOrderMapper.selectById(log.getBizId());
                yield order == null ? "ORDER-" + log.getBizId() : order.getOrderNo();
            }
            case "COMPLAINT" -> {
                Complaint complaint = complaintMapper.selectById(log.getBizId());
                yield complaint == null ? "COMPLAINT-" + log.getBizId() : complaint.getComplaintNo();
            }
            default -> log.getBizType() + "-" + log.getBizId();
        };
    }

    private String resolveOperatorName(OperationLog log) {
        if ("ADMIN".equals(log.getOperatorType())) {
            AdminAccount admin = adminAccountMapper.selectById(log.getOperatorId());
            return admin == null ? "管理员" : admin.getUsername();
        }

        UserAccount user = userAccountMapper.selectById(log.getOperatorId());
        return user == null ? "系统" : user.getNickname();
    }

    private UserAccount requireUser(Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return user;
    }

    private GroupOrder requireOrder(Long orderId) {
        GroupOrder order = groupOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    private Complaint requireComplaint(Long complaintId) {
        Complaint complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            throw new BusinessException(ErrorCode.COMPLAINT_NOT_FOUND);
        }
        return complaint;
    }

    private String normalizeOrderStatus(String status) {
        if (Objects.equals(status, "CANCELLED")) {
            return "CANCELED";
        }
        return status;
    }

    private String denormalizeOrderStatus(String status) {
        return status;
    }
}
