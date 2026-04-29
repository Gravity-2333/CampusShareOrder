package com.campusshareorder.backend.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserStatus 枚举类单元测试
 */
class UserStatusTest {

    @Test
    void testEnumValues() {
        // 验证枚举值数量
        UserStatus[] values = UserStatus.values();
        assertEquals(2, values.length);
    }

    @Test
    void testActiveStatus() {
        // 测试 ACTIVE 状态
        UserStatus status = UserStatus.ACTIVE;
        
        assertEquals("ACTIVE", status.getCode());
        assertEquals("正常", status.getDesc());
    }

    @Test
    void testBannedStatus() {
        // 测试 BANNED 状态
        UserStatus status = UserStatus.BANNED;
        
        assertEquals("BANNED", status.getCode());
        assertEquals("已封禁", status.getDesc());
    }

    @Test
    void testValueOf() {
        // 测试 valueOf 方法
        assertEquals(UserStatus.ACTIVE, UserStatus.valueOf("ACTIVE"));
        assertEquals(UserStatus.BANNED, UserStatus.valueOf("BANNED"));
    }

    @Test
    void testEnumToString() {
        // 测试 toString 方法
        assertEquals("ACTIVE", UserStatus.ACTIVE.toString());
        assertEquals("BANNED", UserStatus.BANNED.toString());
    }

    @Test
    void testAllStatusesHaveCodeAndDesc() {
        // 验证所有状态都有 code 和 desc
        for (UserStatus status : UserStatus.values()) {
            assertNotNull(status.getCode());
            assertNotNull(status.getDesc());
            assertFalse(status.getCode().isEmpty());
            assertFalse(status.getDesc().isEmpty());
        }
    }

    @Test
    void testStatusDistinctness() {
        // 验证所有状态都是不同的
        assertNotEquals(UserStatus.ACTIVE, UserStatus.BANNED);
        assertNotEquals(UserStatus.ACTIVE.getCode(), UserStatus.BANNED.getCode());
        assertNotEquals(UserStatus.ACTIVE.getDesc(), UserStatus.BANNED.getDesc());
    }
}