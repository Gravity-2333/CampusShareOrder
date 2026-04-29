package com.campusshareorder.backend.dto.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JoinOrderRequest DTO 类单元测试
 */
class JoinOrderRequestTest {

    @Test
    void testDtoFields() {
        // 创建测试对象
        JoinOrderRequest request = new JoinOrderRequest();
        
        // 设置字段值
        request.setRemark("我要加入这个拼单");
        
        // 验证字段值
        assertEquals("我要加入这个拼单", request.getRemark());
    }

    @Test
    void testEqualsAndHashCode() {
        // 创建两个相同的对象
        JoinOrderRequest request1 = new JoinOrderRequest();
        request1.setRemark("测试备注");
        
        JoinOrderRequest request2 = new JoinOrderRequest();
        request2.setRemark("测试备注");
        
        // 验证
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testNotEquals() {
        // 创建两个不同的对象
        JoinOrderRequest request1 = new JoinOrderRequest();
        request1.setRemark("备注1");
        
        JoinOrderRequest request2 = new JoinOrderRequest();
        request2.setRemark("备注2");
        
        assertNotEquals(request1, request2);
    }

    @Test
    void testToString() {
        // 创建测试对象
        JoinOrderRequest request = new JoinOrderRequest();
        request.setRemark("测试备注");
        
        // 验证
        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("测试备注"));
    }

    @Test
    void testNullField() {
        // 创建测试对象（不设置任何字段
        JoinOrderRequest request = new JoinOrderRequest();
        
        // 验证字段为 null
        assertNull(request.getRemark());
    }

    @Test
    void testEmptyString() {
        // 创建测试对象
        JoinOrderRequest request = new JoinOrderRequest();
        request.setRemark("");
        
        // 验证
        assertEquals("", request.getRemark());
    }
}