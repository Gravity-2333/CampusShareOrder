package com.campusshareorder.backend.common.constant;

/**
 * 响应码常量类
 */
public class ResponseCode {

    /**
     * 成功
     */
    public static final int SUCCESS = 0;
    public static final String SUCCESS_MESSAGE = "success";

    /**
     * 通用错误
     */
    public static final int ERROR = 1;
    public static final String ERROR_MESSAGE = "操作失败";

    /**
     * 参数错误
     */
    public static final int ERROR_PARAM = 400;
    public static final String ERROR_PARAM_MESSAGE = "参数错误";

    /**
     * 未授权
     */
    public static final int ERROR_UNAUTHORIZED = 401;
    public static final String ERROR_UNAUTHORIZED_MESSAGE = "未授权";

    /**
     * 禁止访问
     */
    public static final int ERROR_FORBIDDEN = 403;
    public static final String ERROR_FORBIDDEN_MESSAGE = "禁止访问";

    /**
     * 资源不存在
     */
    public static final int ERROR_NOT_FOUND = 404;
    public static final String ERROR_NOT_FOUND_MESSAGE = "资源不存在";

    /**
     * 服务器内部错误
     */
    public static final int ERROR_INTERNAL = 500;
    public static final String ERROR_INTERNAL_MESSAGE = "服务器内部错误";

    /**
     * 业务错误（自定义）
     */
    public static final int ERROR_BUSINESS = 1000;
    public static final String ERROR_BUSINESS_MESSAGE = "业务处理失败";

    /**
     * 用户相关错误
     */
    public static final int ERROR_USER_NOT_FOUND = 1001;
    public static final String ERROR_USER_NOT_FOUND_MESSAGE = "用户不存在";

    public static final int ERROR_USER_DISABLED = 1002;
    public static final String ERROR_USER_DISABLED_MESSAGE = "用户已被禁用";

    public static final int ERROR_USER_BANNED = 1003;
    public static final String ERROR_USER_BANNED_MESSAGE = "用户已被封禁";

    /**
     * 订单相关错误
     */
    public static final int ERROR_ORDER_NOT_FOUND = 2001;
    public static final String ERROR_ORDER_NOT_FOUND_MESSAGE = "订单不存在";

    public static final int ERROR_ORDER_STATUS = 2002;
    public static final String ERROR_ORDER_STATUS_MESSAGE = "订单状态不正确";

    public static final int ERROR_ORDER_FULL = 2003;
    public static final String ERROR_ORDER_FULL_MESSAGE = "订单已满员";

    public static final int ERROR_ORDER_EXPIRED = 2004;
    public static final String ERROR_ORDER_EXPIRED_MESSAGE = "订单已过期";

    /**
     * 支付相关错误
     */
    public static final int ERROR_PAY_FAILED = 3001;
    public static final String ERROR_PAY_FAILED_MESSAGE = "支付失败";

    public static final int ERROR_PAY_TIMEOUT = 3002;
    public static final String ERROR_PAY_TIMEOUT_MESSAGE = "支付超时";

    /**
     * 投诉相关错误
     */
    public static final int ERROR_COMPLAINT_NOT_FOUND = 4001;
    public static final String ERROR_COMPLAINT_NOT_FOUND_MESSAGE = "投诉不存在";

    public static final int ERROR_COMPLAINT_EXISTS = 4002;
    public static final String ERROR_COMPLAINT_EXISTS_MESSAGE = "已存在进行中的投诉";
}