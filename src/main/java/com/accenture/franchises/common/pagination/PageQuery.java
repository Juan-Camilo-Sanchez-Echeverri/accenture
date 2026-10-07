package com.accenture.franchises.common.pagination;

public record PageQuery(int page, int limit) {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    public static PageQuery of(int page, int limit) {
        return new PageQuery(Math.max(0, page), Math.max(1, Math.min(limit, MAX_LIMIT)));
    }
}
