package com.campusshareorder.backend.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GroupOrderMember 实体类单元测试
 */
class GroupOrderMemberTest {

    @Test
    void testEntityFields() {
        // 创建测试对象
        GroupOrderMember member = new GroupOrderMember();
        LocalDateTime now = LocalDateTime.now();
        
        // 设置字段值
        member.setId(1L);
        member.setGroupOrderId(100L);
        member.setUserId(200L);
        member.setIsCreator(true);
        member.setRemark("测试备注");
        member.setJoinStatus("已加入");
        member.setPayStatus("已支付");
        member.setPayAmount(new BigDecimal("10.00"));
        member.setPaidAt(now);
        member.setRefundAmountTotal(new BigDecimal("0.00"));
        member.setReceiveStatus("已收货");
        member.setReceivedAt(now);
        member.setRole("参与者");
        member.setCreatedAt(now);
        member.setUpdatedAt(now);
        
        // 验证字段值
        assertEquals(1L, member.getId());
        assertEquals(100L, member.getGroupOrderId());
        assertEquals(200L, member.getUserId());
        assertTrue(member.getIsCreator());
        assertEquals("测试备注", member.getRemark());
        assertEquals("已加入", member.getJoinStatus());
        assertEquals("已支付", member.getPayStatus());
        assertEquals(new BigDecimal("10.00"), member.getPayAmount());
        assertEquals(now, member.getPaidAt());
        assertEquals(new BigDecimal("0.00"), member.getRefundAmountTotal());
        assertEquals("已收货", member.getReceiveStatus());
        assertEquals(now, member.getReceivedAt());
        assertEquals("参与者", member.getRole());
        assertEquals(now, member.getCreatedAt());
        assertEquals(now, member.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        // 创建两个相同的对象
        GroupOrderMember member1 = new GroupOrderMember();
        member1.setId(1L);
        member1.setGroupOrderId(100L);
        member1.setUserId(200L);
        
        GroupOrderMember member2 = new GroupOrderMember();
        member2.setId(1L);
        member2.setGroupOrderId(100L);
        member2.setUserId(200L);
        
        assertEquals(member1, member2);
        assertEquals(member1.hashCode(), member2.hashCode());
    }

    @Test
    void testNotEquals() {
        // 创建两个不同的对象
        GroupOrderMember member1 = new GroupOrderMember();
        member1.setId(1L);
        
        GroupOrderMember member2 = new GroupOrderMember();
        member2.setId(2L);
        
        assertNotEquals(member1, member2);
    }

    @Test
    void testToString() {
        // 创建测试对象
        GroupOrderMember member = new GroupOrderMember();
        member.setId(1L);
        member.setUserId(200L);
        
        String toString = member.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("200"));
    }

    @Test
    void testIsCreator() {
        // 创建测试对象
        GroupOrderMember member = new GroupOrderMember();
        
        // 测试是否为创建者
        member.setIsCreator(true);
        assertTrue(member.getIsCreator());
        
        member.setIsCreator(false);
        assertFalse(member.getIsCreator());
    }

    @Test
    void testAmountFields() {
        // 创建测试对象
        GroupOrderMember member = new GroupOrderMember();
        
        // 测试金额字段
        member.setPayAmount(new BigDecimal("15.50"));
        member.setRefundAmountTotal(new BigDecimal("5.50"));
        
        assertEquals(new BigDecimal("15.50"), member.getPayAmount());
        assertEquals(new BigDecimal("5.50"), member.getRefundAmountTotal());
    }
}