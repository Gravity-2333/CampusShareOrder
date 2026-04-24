package com.campusshareorder.backend.common.response;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private int page;
    private int pageSize;
    private long total;
    private int pages;

    public PageResult() {
    }

    public PageResult(List<T> list, int page, int pageSize, long total) {
        this.list = list;
        this.page = Math.max(page, 1);
        this.pageSize = pageSize < 1 ? 10 : Math.min(pageSize, 100);
        this.total = Math.max(total, 0);
        this.pages = (int) Math.ceil((double) this.total / this.pageSize);
    }
}
