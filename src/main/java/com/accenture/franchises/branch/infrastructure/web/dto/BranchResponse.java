package com.accenture.franchises.branch.infrastructure.web.dto;

import com.accenture.franchises.branch.domain.Branch;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "BranchResponse")
public record BranchResponse(
        @Schema(description = "Identificador único de la sucursal.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID id,
        @Schema(description = "Nombre de la sucursal.", example = "Centro") String name,
        @Schema(description = "Identificador de la franquicia a la que pertenece.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID franchiseId,
        @Schema(description = "Fecha y hora de creación.", example = "2026-01-01T10:00:00Z") Instant createdAt,
        @Schema(description = "Fecha y hora de la última modificación.", example = "2026-01-01T10:00:00Z")
                Instant updatedAt) {

    public static BranchResponse from(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getFranchiseId(),
                branch.getCreatedAt(),
                branch.getUpdatedAt());
    }
}