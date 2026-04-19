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
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }
}