package com.accenture.franchises.franchise.infrastructure.web.dto;

import com.accenture.franchises.franchise.domain.Franchise;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "FranchiseResponse")
public record FranchiseResponse(
        @Schema(description = "Identificador único de la franquicia.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID id,
        @Schema(description = "Nombre de la franquicia.", example = "Acme") String name,
        @Schema(description = "Fecha y hora de creación.", example = "2026-01-01T10:00:00Z") Instant createdAt,
        @Schema(description = "Fecha y hora de la última modificación.", example = "2026-01-01T10:00:00Z")
                Instant updatedAt) {

    public static FranchiseResponse from(Franchise franchise) {
        return new FranchiseResponse(
                franchise.getId(),
                franchise.getName(),
                franchise.getCreatedAt(),
                franchise.getUpdatedAt());
    }
}
