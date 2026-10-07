package com.accenture.franchises.common.infrastructure.web.dto;

import com.accenture.franchises.common.pagination.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;

@Schema(name = "PageResponse")
public record PageResponse<T>(
        @Schema(description = "Elementos de la página consultada.") List<T> items,
        @Schema(description = "Página consultada, empezando en 0.", example = "0") int page,
        @Schema(description = "Elementos por página.", example = "20") int limit,
        @Schema(description = "Total de elementos encontrados.", example = "10") long totalElements,
        @Schema(description = "Total de páginas disponibles.", example = "1") int totalPages) {

    public static <T, R> PageResponse<R> from(PageResult<T> result, Function<T, R> mapper) {
        return new PageResponse<>(
                result.items().stream().map(mapper).toList(),
                result.page(),
                result.limit(),
                result.totalElements(),
                result.totalPages());
    }
}
