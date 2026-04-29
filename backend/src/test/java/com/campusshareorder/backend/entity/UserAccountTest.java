package com.campusshareorder.backend.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserAccount 实体类单元测试
 */
class UserAccountTest {

    @Test
    void testEntityFields() {
        // 创建测试对象
        UserAccount user = new UserAccount();
        LocalDateTime now = LocalDateTime.now();
        
        // 设置字段值
        user.setId(1L);
        user.setPhone("13800138000");
        user.setPasswordHash("hashed_password");
        user.setNickname("测试用户");
        user.setStudentNo("20210001");
        user.setIsVerified(true);
        user.setCreditScore(100);
        user.setStatus("正常");
        user.setContactInfo("qq:123456");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // 验证字段值
        assertEquals(1L, user.getId());
        assertEquals("13800138000", user.getPhone());
        assertEquals("hashed_password", user.getPasswordHash());
        assertEquals("测试用户", user.getNickname());
        assertEquals("20210001", user.getStudentNo());
        assertTrue(user.getIsVerified());
        assertEquals(100, user.getCreditScore());
        assertEquals("正常", user.getStatus());
        assertEquals("qq:123456", user.getContactInfo());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        // 创建两个相同的对象
        UserAccount user1 = new UserAccount();
        user1.setId(1L);
        user1.setPhone("13800138000");
        
        UserAccount user2 = new UserAccount();
        user2.setId(1L);
        user2.setPhone("13800138000");
        
        // 验证
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testNotEquals() {
        // 创建两个不同的对象
        UserAccount user1 = new UserAccount();
        user1.setId(1L);
        
        UserAccount user2 = new UserAccount();
        user2.setId(2L);
        
        assertNotEquals(user1, user2);
    }

    @Test
    void testToString() {
        // 创建测试对象
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setNickname("测试用户");
        
        // 验证
        String toString = user.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("测试用户"));
    }

    @Test
    void testCreditScoreRange() {
        // 创建测试对象
        UserAccount user = new UserAccount();
        user.setCreditScore(80);
        
        // 验证信用分范围
        assertTrue(user.getCreditScore() >= 0);
        assertTrue(user.getCreditScore() <= 100);
    }

    @Test
    void testBooleanFields() {
        // 创建测试对象
        UserAccount user = new UserAccount();
        
        // 测试布尔字段
        user.setIsVerified(true);
        assertTrue(user.getIsVerified());
        
        user.setIsVerified(false);
        assertFalse(user.getIsVerified());
    }
}