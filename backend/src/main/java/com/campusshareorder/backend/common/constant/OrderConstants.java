package com.campusshareorder.backend.common.constant;

/**
 * 订单相关常量
 */
public class OrderConstants {

    /**
     * 默认订单有效期（小时）
     */
    public static final int DEFAULT_ORDER_VALID_HOURS = 24;

    /**
     * 默认成团人数
     */
    public static final int DEFAULT_MIN_MEMBER_COUNT = 2;

    /**
     * 最大订单人数限制
     */
    public static final int MAX_MEMBER_COUNT = 20;

    /**
     * 最小订单人数限制
     */
    public static final int MIN_MEMBER_COUNT = 2;

    /**
     * 订单超时时间（分钟）
     */
    public static final int ORDER_TIMEOUT_MINUTES = 30;

    /**
     * 自动成团检查间隔（分钟）
     */
    public static final int AUTO_GROUP_CHECK_INTERVAL_MINUTES = 5;

    /**
     * 超时取消检查间隔（分钟）
     */
    public static final int TIMEOUT_CANCEL_CHECK_INTERVAL_MINUTES = 3;

    /**
     * 收货超时时间（天）
     */
    public static final int RECEIVE_TIMEOUT_DAYS = 7;

    /**
     * 上传凭证超时时间（小时）
     */
    public static final int UPLOAD_RECEIPT_TIMEOUT_HOURS = 12;

    /**
     * 订单号前缀
     */
    public static final String ORDER_NO_PREFIX = "ORDER";

    /**
     * 拼单状态
     */
    public static final String STATUS_OPEN = "拼单中";
    public static final String STATUS_GROUPED = "已成团";
    public static final String STATUS_WAIT_DELIVERY = "待发货";
    public static final String STATUS_WAIT_RECEIVE = "待收货";
    public static final String STATUS_COMPLETED = "已完成";
    public static final String STATUS_CANCELED = "已取消";

    /**
     * 支付状态
     */
    public static final String PAY_STATUS_UNPAID = "待支付";
    public static final String PAY_STATUS_PAID = "已支付";
    public static final String PAY_STATUS_REFUNDED = "已退款";

    /**
     * 收货状态
     */
    public static final String RECEIVE_STATUS_UNRECEIVED = "待收货";
    public static final String RECEIVE_STATUS_RECEIVED = "已收货";

    /**
     * 加入状态
     */
    public static final String JOIN_STATUS_JOINED = "已加入";
    public static final String JOIN_STATUS_LEFT = "已退出";
}