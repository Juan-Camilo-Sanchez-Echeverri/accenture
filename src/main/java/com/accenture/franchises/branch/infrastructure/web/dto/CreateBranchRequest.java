package com.accenture.franchises.branch.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateBranchRequest", description = "Datos de alta de una sucursal.")
public record CreateBranchRequest(
        @NotBlank(message = "must not be blank")
                @Size(max = 100, message = "must have at most 100 characters")
                @Schema(description = "Nombre de la sucursal.", example = "Centro", maxLength = 100, 
                                requiredMode = Schema.RequiredMode.REQUIRED)
                String name) {
}