package com.accenture.franchises.franchise.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RenameFranchiseRequest", description = "Nuevo nombre de una franquicia.")
public record RenameFranchiseRequest(
        @NotBlank(message = "must not be blank")
                @Size(max = 120, message = "must have at most 120 characters")
                @Schema(description = "Nuevo nombre de la franquicia.", example = "Acme Corp", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
                String name) {
}
