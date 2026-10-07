package com.accenture.franchises.product.infrastructure.web.dto;

import com.accenture.franchises.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(name = "ProductResponse")
public record ProductResponse(
        @Schema(description = "Identificador único del producto.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID id,
        @Schema(description = "Nombre del producto.", example = "Hamburguesa") String name,
        @Schema(description = "Fecha y hora de creación.", example = "2026-01-01T10:00:00Z") Instant createdAt,
        @Schema(description = "Fecha y hora de la última modificación.", example = "2026-01-01T10:00:00Z")
                Instant updatedAt,
        @Schema(description = "Stock del producto en cada sucursal.")
                List<BranchStockResponse> stocks) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getStocks().stream().map(BranchStockResponse::from).toList());
    }
}