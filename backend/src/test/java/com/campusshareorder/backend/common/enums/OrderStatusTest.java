package com.campusshareorder.backend.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderStatus 枚举类单元测试
 */
class OrderStatusTest {

    @Test
    void testEnumValues() {
        // 验证枚举值数量
        OrderStatus[] values = OrderStatus.values();
        assertEquals(6, values.length);
    }

    @Test
    void testOpenStatus() {
        // 测试 OPEN 状态
        OrderStatus status = OrderStatus.OPEN;
        
        assertEquals("OPEN", status.getCode());
        assertEquals("待成团", status.getDesc());
    }

    @Test
    void testGroupedStatus() {
        // 测试 GROUPED 状态
        OrderStatus status = OrderStatus.GROUPED;
        
        assertEquals("GROUPED", status.getCode());
        assertEquals("已成团", status.getDesc());
    }

    @Test
    void testWaitDeliveryStatus() {
        // 测试 WAIT_DELIVERY 状态
        OrderStatus status = OrderStatus.WAIT_DELIVERY;
        
        assertEquals("WAIT_DELIVERY", status.getCode());
        assertEquals("待发货", status.getDesc());
    }

    @Test
    void testWaitReceiveStatus() {
        // 测试 WAIT_RECEIVE 状态
        OrderStatus status = OrderStatus.WAIT_RECEIVE;
        
        assertEquals("WAIT_RECEIVE", status.getCode());
        assertEquals("待收货", status.getDesc());
    }

    @Test
    void testCompletedStatus() {
        // 测试 COMPLETED 状态
        OrderStatus status = OrderStatus.COMPLETED;
        
        assertEquals("COMPLETED", status.getCode());
        assertEquals("已完成", status.getDesc());
    }

    @Test
    void testCanceledStatus() {
        // 测试 CANCELED 状态
        OrderStatus status = OrderStatus.CANCELED;
        
        assertEquals("CANCELED", status.getCode());
        assertEquals("已取消", status.getDesc());
    }

    @Test
    void testValueOf() {
        // 测试 valueOf 方法
        assertEquals(OrderStatus.OPEN, OrderStatus.valueOf("OPEN"));
        assertEquals(OrderStatus.GROUPED, OrderStatus.valueOf("GROUPED"));
        assertEquals(OrderStatus.COMPLETED, OrderStatus.valueOf("COMPLETED"));
    }

    @Test
    void testEnumToString() {
        // 测试 toString 方法
        assertEquals("OPEN", OrderStatus.OPEN.toString());
        assertEquals("COMPLETED", OrderStatus.COMPLETED.toString());
    }

    @Test
    void testAllStatusesHaveCodeAndDesc() {
        // 验证所有状态都有 code 和 desc
        for (OrderStatus status : OrderStatus.values()) {
            assertNotNull(status.getCode());
            assertNotNull(status.getDesc());
            assertFalse(status.getCode().isEmpty());
            assertFalse(status.getDesc().isEmpty());
        }
    }
}