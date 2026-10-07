package com.accenture.franchises.common.pagination;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int limit, long totalElements, int totalPages) {

    public static <T> PageResult<T> of(List<T> items, int page, int limit, long totalElements) {
        return new PageResult<>(items, page, limit, totalElements, totalPages(totalElements, limit));
    }

    private static int totalPages(long totalElements, int limit) {
        return (int) ((totalElements + limit - 1) / limit);
    }
}
