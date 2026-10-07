package com.accenture.franchises.franchise.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateFranchiseRequest", description = "Datos de alta de una franquicia.")
public record CreateFranchiseRequest(
        @NotBlank(message = "must not be blank")
                @Size(max = 120, message = "must have at most 120 characters")
                @Schema(description = "Nombre de la franquicia.", example = "Acme", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
                String name) {
}
