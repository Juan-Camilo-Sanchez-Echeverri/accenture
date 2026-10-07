package com.accenture.franchises.branch.infrastructure.web.dto;

import com.accenture.franchises.branch.domain.BranchProductSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "BranchProductSummaryResponse")
public record BranchProductSummaryResponse(
        @Schema(description = "Identificador único del producto.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID productId,
        @Schema(description = "Nombre del producto.", example = "Big Mac") String productName,
        @Schema(description = "Cantidad en stock del producto en la sucursal.", example = "15") int stock) {

    public static BranchProductSummaryResponse from(BranchProductSummary summary) {
        return new BranchProductSummaryResponse(
                summary.productId(),
                summary.productName(),
                summary.stock());
    }
}