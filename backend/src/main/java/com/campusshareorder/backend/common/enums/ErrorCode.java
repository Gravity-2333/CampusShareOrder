package com.campusshareorder.backend.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNAUTHORIZED(40101, "用户未登录"),
    TOKEN_INVALID(40102, "登录状态已失效"),
    FORBIDDEN(40301, "无权限访问"),
    USER_BANNED(40302, "账号已被封禁"),
    USER_NOT_VERIFIED(40303, "请先完成实名认证"),
    ORDER_NOT_FOUND(40401, "订单不存在"),
    COMPLAINT_NOT_FOUND(40402, "投诉记录不存在"),
    ORDER_FULL(40901, "订单已满员"),
    ORDER_ALREADY_JOINED(40902, "您已经加入该订单"),
    ORDER_STATUS_INVALID(40903, "当前订单状态不允许执行该操作"),
    ORDER_MEMBER_REQUIRED(40904, "当前账号未加入该订单"),
    ORDER_ALREADY_PAID(40905, "当前账号已完成支付"),
    ORDER_UNPAID(40906, "当前账号尚未支付"),
    RECEIPT_ALREADY_UPLOADED(40907, "订单已上传凭证"),
    INITIATOR_EXIT_NOT_ALLOWED(40908, "发起人不能退出订单"),
    COMPLAINT_SELF_NOT_ALLOWED(40909, "不能投诉自己"),
    COMPLAINT_ALREADY_PROCESSED(40910, "投诉已处理"),
    COMPLAINT_DUPLICATED(40913, "请勿重复投诉"),
    VALIDATION_ERROR(42201, "参数校验错误"),
    SYSTEM_ERROR(50000, "系统繁忙，请稍后再试");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
