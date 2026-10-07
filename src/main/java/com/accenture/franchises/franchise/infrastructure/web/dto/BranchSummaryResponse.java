package com.accenture.franchises.franchise.infrastructure.web.dto;

import com.accenture.franchises.branch.domain.Branch;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "BranchSummaryResponse")
public record BranchSummaryResponse(
        @Schema(description = "Identificador único de la sucursal.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f")
                UUID id,
        @Schema(description = "Nombre de la sucursal.", example = "Centro") String name) {

    public static BranchSummaryResponse from(Branch branch) {
        return new BranchSummaryResponse(branch.getId(), branch.getName());
    }
}