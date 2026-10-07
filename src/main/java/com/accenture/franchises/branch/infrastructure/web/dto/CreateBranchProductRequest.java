package com.accenture.franchises.branch.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(name = "CreateBranchProductRequest", description = "Datos para vincular un producto a una sucursal.")
public record CreateBranchProductRequest(
        @NotNull(message = "must not be null")
                @Schema(description = "Identificador del producto del catálogo.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f", requiredMode = Schema.RequiredMode.REQUIRED)
                UUID productId,
        @NotNull(message = "must not be null")
                @Min(value = 0, message = "must be greater than or equal to 0")
                @Schema(description = "Stock inicial del producto en la sucursal.", example = "10", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
                Integer stock) {
}