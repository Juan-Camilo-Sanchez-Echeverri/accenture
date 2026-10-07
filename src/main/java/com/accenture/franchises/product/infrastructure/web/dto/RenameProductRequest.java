package com.accenture.franchises.product.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RenameProductRequest", description = "Nuevo nombre de un producto.")
public record RenameProductRequest(
        @NotBlank(message = "must not be blank")
                @Size(max = 120, message = "must have at most 120 characters")
                @Schema(description = "Nuevo nombre del producto.", example = "Hamburguesa XL", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
                String name) {
}