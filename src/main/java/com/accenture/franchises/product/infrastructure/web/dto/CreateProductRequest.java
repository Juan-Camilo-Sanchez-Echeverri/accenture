package com.accenture.franchises.product.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateProductRequest", description = "Datos de alta de un producto del catálogo.")
public record CreateProductRequest(
        @NotBlank(message = "must not be blank")
                @Size(max = 120, message = "must have at most 120 characters")
                @Schema(description = "Nombre del producto.", example = "Hamburguesa", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
                String name) {
}