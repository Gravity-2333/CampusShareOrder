package com.campusshareorder.backend.common.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiResponse 响应类单元测试
 */
class ApiResponseTest {

    @Test
    void testConstructor() {
        // 测试带参构造器
        ApiResponse<String> response = new ApiResponse<>(0, "测试消息", "测试数据");
        
        assertEquals(0, response.getCode());
        assertEquals("测试消息", response.getMessage());
        assertEquals("测试数据", response.getData());
    }

    @Test
    void testSuccessWithData() {
        // 测试成功响应（带数据
        ApiResponse<String> response = ApiResponse.success("测试数据");
        
        assertEquals(0, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("测试数据", response.getData());
    }

    @Test
    void testSuccessWithoutData() {
        // 测试成功响应（无数据
        ApiResponse<String> response = ApiResponse.success();
        
        assertEquals(0, response.getCode());
        assertEquals("success", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testErrorWithCode() {
        // 测试错误响应（带错误码
        ApiResponse<String> response = ApiResponse.error(500, "服务器错误");
        
        assertEquals(500, response.getCode());
        assertEquals("服务器错误", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testErrorWithoutCode() {
        // 测试错误响应（不带错误码
        ApiResponse<String> response = ApiResponse.error("业务错误");
        
        assertEquals(1, response.getCode());
        assertEquals("业务错误", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testSettersAndGetters() {
        // 测试setter和getter
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("OK");
        response.setData(123);
        
        assertEquals(200, response.getCode());
        assertEquals("OK", response.getMessage());
        assertEquals(123, response.getData());
    }

    @Test
    void testNullData() {
        // 测试null数据
        ApiResponse<String> response = ApiResponse.success(null);
        
        assertEquals(0, response.getCode());
        assertNull(response.getData());
    }

    @Test
    void testEqualsAndHashCode() {
        // 测试equals和hashCode
        ApiResponse<String> response1 = new ApiResponse<>(0, "success", "data");
        ApiResponse<String> response2 = new ApiResponse<>(0, "success", "data");
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString() {
        // 测试toString
        ApiResponse<String> response = ApiResponse.success("测试数据");
        
        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("success"));
    }
}