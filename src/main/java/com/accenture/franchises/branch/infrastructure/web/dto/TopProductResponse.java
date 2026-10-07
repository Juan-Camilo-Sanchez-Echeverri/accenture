package com.accenture.franchises.branch.infrastructure.web.dto;

import com.accenture.franchises.branch.domain.TopProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "TopProductResponse", description = "Producto con mayor stock dentro de una sucursal.")
public record TopProductResponse(
        @Schema(description = "Identificador de la sucursal.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID branchId,
        @Schema(description = "Nombre de la sucursal.", example = "Centro")
                String branchName,
        @Schema(description = "Identificador del producto.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID productId,
        @Schema(description = "Nombre del producto.", example = "Hamburguesa")
                String productName,
        @Schema(description = "Cantidad en stock del producto en la sucursal.", example = "40") int stock) {

    public static TopProductResponse from(TopProduct topProduct) {
        return new TopProductResponse(
                topProduct.branchId(),
                topProduct.branchName(),
                topProduct.productId(),
                topProduct.productName(),
                topProduct.stock());
    }
}