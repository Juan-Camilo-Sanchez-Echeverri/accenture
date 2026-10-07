package com.accenture.franchises.branch.infrastructure.web.dto;

import com.accenture.franchises.branch.domain.BranchProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "ProductStockResponse")
public record ProductStockResponse(
        @Schema(description = "Identificador de la sucursal.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID branchId,
        @Schema(description = "Identificador del producto.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID productId,
        @Schema(description = "Cantidad en stock del producto en la sucursal.", example = "10") int stock) {

    public static ProductStockResponse from(BranchProduct branchProduct) {
        return new ProductStockResponse(
                branchProduct.getBranchId(),
                branchProduct.getProductId(),
                branchProduct.getStock());
    }
}