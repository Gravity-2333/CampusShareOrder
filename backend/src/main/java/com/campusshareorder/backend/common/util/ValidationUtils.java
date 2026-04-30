package com.campusshareorder.backend.common.util;

import java.util.regex.Pattern;

/**
 * 验证工具类
 */
public class ValidationUtils {

    /**
     * 手机号正则
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 邮箱正则
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

    /**
     * 学号正则
     */
    private static final Pattern STUDENT_NO_PATTERN = Pattern.compile("^\\d{8,12}$");

    /**
     * 身份证号正则
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");

    /**
     * 验证手机号
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证邮箱
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证学号
     */
    public static boolean isValidStudentNo(String studentNo) {
        if (studentNo == null || studentNo.isEmpty()) {
            return false;
        }
        return STUDENT_NO_PATTERN.matcher(studentNo).matches();
    }

    /**
     * 验证身份证号
     */
    public static boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return false;
        }
        return ID_CARD_PATTERN.matcher(idCard).matches();
    }

    /**
     * 验证是否为空
     */
    public static boolean isEmpty(Object obj) {
        return obj == null || (obj instanceof String && ((String) obj).trim().isEmpty());
    }

    /**
     * 验证是否不为空
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 验证字符串长度范围
     */
    public static boolean isLengthInRange(String str, int min, int max) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        return length >= min && length <= max;
    }

    /**
     * 验证数字范围
     */
    public static boolean isNumberInRange(Integer number, int min, int max) {
        if (number == null) {
            return false;
        }
        return number >= min && number <= max;
    }
}