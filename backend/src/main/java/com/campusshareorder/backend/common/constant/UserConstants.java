package com.campusshareorder.backend.common.constant;

/**
 * 用户相关常量
 */
public class UserConstants {

    /**
     * 默认信用分
     */
    public static final int DEFAULT_CREDIT_SCORE = 80;

    /**
     * 最高信用分
     */
    public static final int MAX_CREDIT_SCORE = 100;

    /**
     * 最低信用分
     */
    public static final int MIN_CREDIT_SCORE = 0;

    /**
     * 信用分扣减阈值（每次投诉）
     */
    public static final int CREDIT_DEDUCTION_PER_COMPLAINT = 10;

    /**
     * 用户状态
     */
    public static final String STATUS_ACTIVE = "正常";
    public static final String STATUS_BANNED = "已封禁";

    /**
     * 用户角色
     */
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    /**
     * 昵称最大长度
     */
    public static final int NICKNAME_MAX_LENGTH = 20;

    /**
     * 昵称最小长度
     */
    public static final int NICKNAME_MIN_LENGTH = 2;

    /**
     * 手机号长度
     */
    public static final int PHONE_LENGTH = 11;

    /**
     * 学号长度
     */
    public static final int STUDENT_NO_LENGTH = 10;

    /**
     * 密码最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 6;

    /**
     * 密码最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 20;

    /**
     * 验证码长度
     */
    public static final int VERIFY_CODE_LENGTH = 6;

    /**
     * 验证码有效期（分钟）
     */
    public static final int VERIFY_CODE_EXPIRE_MINUTES = 5;
}