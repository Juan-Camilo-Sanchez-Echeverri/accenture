package com.accenture.franchises.franchise.infrastructure.web.dto;

import com.accenture.franchises.franchise.domain.FranchiseDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(name = "FranchiseDetailResponse")
public record FranchiseDetailResponse(
        @Schema(description = "Identificador único de la franquicia.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID id,
        @Schema(description = "Nombre de la franquicia.", example = "Acme") String name,
        @Schema(description = "Sucursales que componen la franquicia, ordenadas por fecha de creación.")
                List<BranchSummaryResponse> branches,
        @Schema(description = "Fecha y hora de creación.", example = "2026-01-01T10:00:00Z") Instant createdAt,
        @Schema(description = "Fecha y hora de la última modificación.", example = "2026-01-01T10:00:00Z")
                Instant updatedAt) {

    public static FranchiseDetailResponse from(FranchiseDetail detail) {
        return new FranchiseDetailResponse(
                detail.franchise().getId(),
                detail.franchise().getName(),
                detail.branches().stream().map(BranchSummaryResponse::from).toList(),
                detail.franchise().getCreatedAt(),
                detail.franchise().getUpdatedAt());
    }
}