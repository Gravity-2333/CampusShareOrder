package com.campusshareorder.backend.common.response;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageResult 分页响应类单元测试
 */
class PageResultTest {

    @Test
    void testConstructor() {
        // 创建测试数据
        List<String> list = new ArrayList<>();
        list.add("项目1");
        list.add("项目2");
        list.add("项目3");
        
        // 创建分页结果
        PageResult<String> pageResult = new PageResult<>(list, 1, 10, 100);
        
        // 验证
        assertEquals(list, pageResult.getList());
        assertEquals(1, pageResult.getPage());
        assertEquals(10, pageResult.getPageSize());
        assertEquals(100, pageResult.getTotal());
        assertEquals(10, pageResult.getPages());
    }

    @Test
    void testPagesCalculation() {
        List<String> list = new ArrayList<>();
        
        // 测试整除情况
        PageResult<String> pageResult1 = new PageResult<>(list, 1, 10, 50);
        assertEquals(5, pageResult1.getPages());
        
        // 测试非整除情况
        PageResult<String> pageResult2 = new PageResult<>(list, 1, 10, 55);
        assertEquals(6, pageResult2.getPages());
        
        // 测试刚好一页
        PageResult<String> pageResult3 = new PageResult<>(list, 1, 10, 5);
        assertEquals(1, pageResult3.getPages());
        
        // 测试空数据
        PageResult<String> pageResult4 = new PageResult<>(list, 1, 10, 0);
        assertEquals(0, pageResult4.getPages());
    }

    @Test
    void testSettersAndGetters() {
        // 创建对象
        PageResult<String> pageResult = new PageResult<>();
        List<String> list = new ArrayList<>();
        list.add("测试");
        
        // 设置值
        pageResult.setList(list);
        pageResult.setPage(2);
        pageResult.setPageSize(20);
        pageResult.setTotal(200);
        pageResult.setPages(10);
        
        // 验证
        assertEquals(list, pageResult.getList());
        assertEquals(2, pageResult.getPage());
        assertEquals(20, pageResult.getPageSize());
        assertEquals(200, pageResult.getTotal());
        assertEquals(10, pageResult.getPages());
    }

    @Test
    void testEmptyList() {
        // 测试空列表
        List<String> emptyList = new ArrayList<>();
        PageResult<String> pageResult = new PageResult<>(emptyList, 1, 10, 0);
        
        assertTrue(pageResult.getList().isEmpty());
        assertEquals(0, pageResult.getPages());
    }

    @Test
    void testEqualsAndHashCode() {
        // 测试equals和hashCode
        List<String> list = new ArrayList<>();
        PageResult<String> result1 = new PageResult<>(list, 1, 10, 50);
        PageResult<String> result2 = new PageResult<>(list, 1, 10, 50);
        
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void testToString() {
        // 测试toString
        List<String> list = new ArrayList<>();
        list.add("测试1");
        list.add("测试2");
        
        PageResult<String> pageResult = new PageResult<>(list, 1, 10, 2);
        
        String toString = pageResult.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("测试1"));
    }
}