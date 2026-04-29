package com.campusshareorder.backend.dto.order;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CreateOrderRequest DTO 类单元测试
 */
class CreateOrderRequestTest {

    @Test
    void testDtoFields() {
        // 创建测试对象
        CreateOrderRequest request = new CreateOrderRequest();
        
        // 设置字段值
        request.setProductName("奶茶拼单");
        request.setProductDesc("三分糖，少冰");
        request.setTotalMemberCount(3);
        request.setEstimatedTotalAmount(new BigDecimal("30.00"));
        request.setPickupPoint("图书馆门口");
        request.setDeadlineAt("2026-04-30T12:00:00");
        
        // 验证字段值
        assertEquals("奶茶拼单", request.getProductName());
        assertEquals("三分糖，少冰", request.getProductDesc());
        assertEquals(3, request.getTotalMemberCount());
        assertEquals(new BigDecimal("30.00"), request.getEstimatedTotalAmount());
        assertEquals("图书馆门口", request.getPickupPoint());
        assertEquals("2026-04-30T12:00:00", request.getDeadlineAt());
    }

    @Test
    void testEqualsAndHashCode() {
        // 创建两个相同的对象
        CreateOrderRequest request1 = new CreateOrderRequest();
        request1.setProductName("奶茶拼单");
        request1.setTotalMemberCount(3);
        
        CreateOrderRequest request2 = new CreateOrderRequest();
        request2.setProductName("奶茶拼单");
        request2.setTotalMemberCount(3);
        
        // 验证
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testNotEquals() {
        // 创建两个不同的对象
        CreateOrderRequest request1 = new CreateOrderRequest();
        request1.setTotalMemberCount(3);
        
        CreateOrderRequest request2 = new CreateOrderRequest();
        request2.setTotalMemberCount(5);
        
        assertNotEquals(request1, request2);
    }

    @Test
    void testToString() {
        // 创建测试对象
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductName("奶茶拼单");
        request.setTotalMemberCount(3);
        
        // 验证
        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("奶茶拼单"));
    }

    @Test
    void testNullFields() {
        // 创建测试对象（不设置任何字段
        CreateOrderRequest request = new CreateOrderRequest();
        
        // 验证所有字段都是 null
        assertNull(request.getProductName());
        assertNull(request.getProductDesc());
        assertNull(request.getTotalMemberCount());
        assertNull(request.getEstimatedTotalAmount());
        assertNull(request.getPickupPoint());
        assertNull(request.getDeadlineAt());
    }

    @Test
    void testBigDecimalHandling() {
        // 创建测试对象
        CreateOrderRequest request = new CreateOrderRequest();
        
        // 测试 BigDecimal
        BigDecimal amount = new BigDecimal("99.99");
        request.setEstimatedTotalAmount(amount);
        
        assertEquals(amount, request.getEstimatedTotalAmount());
    }
}