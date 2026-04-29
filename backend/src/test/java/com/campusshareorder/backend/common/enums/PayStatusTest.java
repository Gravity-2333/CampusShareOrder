package com.campusshareorder.backend.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PayStatus 枚举类单元测试
 */
class PayStatusTest {

    @Test
    void testEnumValues() {
        // 验证枚举值数量
        PayStatus[] values = PayStatus.values();
        assertEquals(3, values.length);
    }

    @Test
    void testUnpaidStatus() {
        // 测试 UNPAID 状态
        PayStatus status = PayStatus.UNPAID;
        
        assertEquals("UNPAID", status.getCode());
        assertEquals("未支付", status.getDesc());
    }

    @Test
    void testPaidStatus() {
        // 测试 PAID 状态
        PayStatus status = PayStatus.PAID;
        
        assertEquals("PAID", status.getCode());
        assertEquals("已支付", status.getDesc());
    }

    @Test
    void testRefundedStatus() {
        // 测试 REFUNDED 状态
        PayStatus status = PayStatus.REFUNDED;
        
        assertEquals("REFUNDED", status.getCode());
        assertEquals("已退款", status.getDesc());
    }

    @Test
    void testValueOf() {
        // 测试 valueOf 方法
        assertEquals(PayStatus.UNPAID, PayStatus.valueOf("UNPAID"));
        assertEquals(PayStatus.PAID, PayStatus.valueOf("PAID"));
        assertEquals(PayStatus.REFUNDED, PayStatus.valueOf("REFUNDED"));
    }

    @Test
    void testEnumToString() {
        // 测试 toString 方法
        assertEquals("UNPAID", PayStatus.UNPAID.toString());
        assertEquals("PAID", PayStatus.PAID.toString());
    }

    @Test
    void testAllStatusesHaveCodeAndDesc() {
        // 验证所有状态都有 code 和 desc
        for (PayStatus status : PayStatus.values()) {
            assertNotNull(status.getCode());
            assertNotNull(status.getDesc());
            assertFalse(status.getCode().isEmpty());
            assertFalse(status.getDesc().isEmpty());
        }
    }
}