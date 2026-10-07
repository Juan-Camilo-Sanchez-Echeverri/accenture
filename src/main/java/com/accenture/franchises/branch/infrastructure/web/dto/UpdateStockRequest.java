package com.accenture.franchises.branch.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateStockRequest", description = "Datos para cambiar el stock de un producto en una sucursal.")
public record UpdateStockRequest(
        @NotNull(message = "must not be null")
                @Min(value = 0, message = "must be greater than or equal to 0")
                @Schema(description = "Nuevo stock del producto en la sucursal.", example = "25", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
                Integer stock) {
}