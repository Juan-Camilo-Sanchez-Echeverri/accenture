package com.accenture.franchises.product.infrastructure.web.dto;

import com.accenture.franchises.product.domain.ProductStock;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "BranchStockResponse")
public record BranchStockResponse(
        @Schema(description = "Identificador de la sucursal.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID branchId,
        @Schema(description = "Nombre de la sucursal.", example = "Centro") String branchName,
        @Schema(description = "Cantidad en stock del producto en la sucursal.", example = "10") int stock) {

    public static BranchStockResponse from(ProductStock productStock) {
        return new BranchStockResponse(
                productStock.branchId(),
                productStock.branchName(),
                productStock.stock());
    }
}