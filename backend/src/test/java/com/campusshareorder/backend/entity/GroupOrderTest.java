package com.campusshareorder.backend.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GroupOrder 实体类单元测试
 */
class GroupOrderTest {

    @Test
    void testEntityFields() {
        // 创建测试对象
        GroupOrder order = new GroupOrder();
        
        // 设置字段值
        LocalDateTime now = LocalDateTime.now();
        order.setId(1L);
        order.setOrderNo("TEST001");
        order.setCreatorUserId(100L);
        order.setProductName("测试商品");
        order.setProductDesc("测试描述");
        order.setTotalMemberCount(3);
        order.setCurrentMemberCount(1);
        order.setEstimatedTotalAmount(new BigDecimal("30.00"));
        order.setEstimatedPerAmount(new BigDecimal("10.00"));
        order.setPickupPoint("测试地点");
        order.setDeadlineAt(now);
        order.setStatus("拼单中");
        order.setComplaintOpened(false);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        
        // 验证字段值
        assertEquals(1L, order.getId());
        assertEquals("TEST001", order.getOrderNo());
        assertEquals(100L, order.getCreatorUserId());
        assertEquals("测试商品", order.getProductName());
        assertEquals("测试描述", order.getProductDesc());
        assertEquals(3, order.getTotalMemberCount());
        assertEquals(1, order.getCurrentMemberCount());
        assertEquals(new BigDecimal("30.00"), order.getEstimatedTotalAmount());
        assertEquals(new BigDecimal("10.00"), order.getEstimatedPerAmount());
        assertEquals("测试地点", order.getPickupPoint());
        assertEquals(now, order.getDeadlineAt());
        assertEquals("拼单中", order.getStatus());
        assertFalse(order.getComplaintOpened());
        assertEquals(now, order.getCreatedAt());
        assertEquals(now, order.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        // 创建两个相同的对象
        GroupOrder order1 = new GroupOrder();
        order1.setId(1L);
        order1.setOrderNo("TEST001");
        
        GroupOrder order2 = new GroupOrder();
        order2.setId(1L);
        order2.setOrderNo("TEST001");
        
        // 验证 equals
        assertEquals(order1, order2);
        assertEquals(order1.hashCode(), order2.hashCode());
    }

    @Test
    void testNotEquals() {
        // 创建两个不同的对象
        GroupOrder order1 = new GroupOrder();
        order1.setId(1L);
        
        GroupOrder order2 = new GroupOrder();
        order2.setId(2L);
        
        // 验证
        assertNotEquals(order1, order2);
    }

    @Test
    void testToString() {
        // 创建测试对象
        GroupOrder order = new GroupOrder();
        order.setId(1L);
        order.setOrderNo("TEST001");
        
        // 验证 toString 不为空
        String toString = order.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TEST001"));
    }

    @Test
    void testOptionalFields() {
        // 创建测试对象（设置可选字段
        GroupOrder order = new GroupOrder();
        LocalDateTime now = LocalDateTime.now();
        
        // 设置可选字段
        order.setActualTotalAmount(new BigDecimal("28.50"));
        order.setActualPerAmount(new BigDecimal("9.50"));
        order.setCancelReason("测试取消");
        
        // 验证
        assertEquals(new BigDecimal("28.50"), order.getActualTotalAmount());
        assertEquals(new BigDecimal("9.50"), order.getActualPerAmount());
        assertEquals("测试取消", order.getCancelReason());
    }
}