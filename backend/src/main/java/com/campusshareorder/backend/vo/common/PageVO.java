package com.campusshareorder.backend.vo.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageVO<T> {
    private List<T> list;
    private Long total;
    private Long page;
    private Long pageSize;
    private Long pages;

    public static <T> PageVO<T> of(List<T> list, Long total, Long page, Long pageSize) {
        Long pages = (total + pageSize - 1) / pageSize;
        return new PageVO<>(list, total, page, pageSize, pages);
    }
}
