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
        Long safeTotal = total == null || total < 0 ? 0 : total;
        Long safePage = page == null || page < 1 ? 1 : page;
        Long safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        Long pages = (safeTotal + safePageSize - 1) / safePageSize;
        return new PageVO<>(list, safeTotal, safePage, safePageSize, pages);
    }
}
