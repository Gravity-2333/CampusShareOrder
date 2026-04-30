package com.campusshareorder.backend.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具类
 */
public class MoneyUtils {

    /**
     * 精度位数
     */
    public static final int SCALE = 2;

    /**
     * 四舍五入模式
     */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * 元转分
     */
    public static long yuanToFen(BigDecimal yuan) {
        if (yuan == null) {
            return 0;
        }
        return yuan.multiply(new BigDecimal("100")).longValue();
    }

    /**
     * 分转元
     */
    public static BigDecimal fenToYuan(long fen) {
        return new BigDecimal(fen).divide(new BigDecimal("100"), SCALE, ROUNDING_MODE);
    }

    /**
     * 计算人均金额
     */
    public static BigDecimal calculatePerAmount(BigDecimal totalAmount, int personCount) {
        if (totalAmount == null || personCount <= 0) {
            return BigDecimal.ZERO;
        }
        return totalAmount.divide(new BigDecimal(personCount), SCALE, ROUNDING_MODE);
    }

    /**
     * 格式化金额（保留两位小数）
     */
    public static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(SCALE, ROUNDING_MODE).toString();
    }

    /**
     * 金额加法
     */
    public static BigDecimal add(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null) {
            amount1 = BigDecimal.ZERO;
        }
        if (amount2 == null) {
            amount2 = BigDecimal.ZERO;
        }
        return amount1.add(amount2);
    }

    /**
     * 金额减法
     */
    public static BigDecimal subtract(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null) {
            amount1 = BigDecimal.ZERO;
        }
        if (amount2 == null) {
            amount2 = BigDecimal.ZERO;
        }
        return amount1.subtract(amount2);
    }

    /**
     * 金额乘法
     */
    public static BigDecimal multiply(BigDecimal amount, BigDecimal multiplier) {
        if (amount == null || multiplier == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(multiplier).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 金额除法
     */
    public static BigDecimal divide(BigDecimal amount, BigDecimal divisor) {
        if (amount == null || divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return amount.divide(divisor, SCALE, ROUNDING_MODE);
    }

    /**
     * 判断金额是否大于零
     */
    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断金额是否为零
     */
    public static boolean isZero(BigDecimal amount) {
        return amount == null || amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 判断金额是否小于零
     */
    public static boolean isNegative(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }
}