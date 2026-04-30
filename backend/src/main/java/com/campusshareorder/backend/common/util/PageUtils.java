package com.campusshareorder.backend.common.util;

import com.campusshareorder.backend.common.response.PageResult;

import java.util.List;

/**
 * 分页工具类
 */
public class PageUtils {

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> createPageResult(List<T> list, int page, int pageSize, long total) {
        return new PageResult<>(list, page, pageSize, total);
    }

    /**
     * 计算总页数
     */
    public static int calculateTotalPages(long total, int pageSize) {
        if (total <= 0 || pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 校验页码
     */
    public static int validatePage(int page) {
        if (page < 1) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    /**
     * 校验每页大小
     */
    public static int validatePageSize(int pageSize) {
        if (pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * 计算偏移量
     */
    public static int calculateOffset(int page, int pageSize) {
        return (validatePage(page) - 1) * validatePageSize(pageSize);
    }
}