package com.campusshareorder.backend.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 401xx - 认证相关
    UNAUTHORIZED(40101, "未登录"),
    TOKEN_INVALID(40102, "token无效或已过期"),

    // 403xx - 权限相关
    FORBIDDEN(40301, "无权限"),
    ACCOUNT_BANNED(40302, "账号已封禁"),
    NOT_VERIFIED(40303, "未实名认证"),

    // 404xx - 资源不存在
    ORDER_NOT_FOUND(40401, "订单不存在"),
    COMPLAINT_NOT_FOUND(40402, "投诉不存在"),

    // 409xx - 冲突相关
    ORDER_FULL(40901, "订单已满员"),
    ALREADY_JOINED(40902, "已加入订单"),
    ORDER_STATUS_INVALID(40903, "订单状态不允许当前操作"),
    NOT_JOINED(40904, "未加入订单"),
    ALREADY_PAID(40905, "已支付"),
    NOT_PAID(40906, "未支付"),
    ALREADY_UPLOADED(40907, "已上传凭证"),
    CREATOR_CANNOT_EXIT(40908, "发起人不能退出订单"),
    CREATOR_CANNOT_COMPLAINT(40909, "发起人不能投诉自己"),
    COMPLAINT_ALREADY_HANDLED(40910, "投诉已处理"),
    DUPLICATE_COMPLAINT(40913, "重复投诉"),

    // 422xx - 参数校验错误
    PARAM_VALID_ERROR(42201, "参数校验错误"),

    // 500xx - 系统异常
    SYSTEM_ERROR(50000, "系统异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
