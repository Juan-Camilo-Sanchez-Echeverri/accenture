package com.accenture.franchises.branch.infrastructure.web;

import com.accenture.franchises.branch.application.BranchService;
import com.accenture.franchises.branch.infrastructure.web.dto.BranchResponse;
import com.accenture.franchises.branch.infrastructure.web.dto.CreateBranchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Sucursales", description = "Sucursales de una franquicia.")
public class BranchController {

    private static final String NOT_FOUND = "No existe la franquicia indicada.";
    private static final String INVALID_ID = "El identificador no es un UUID válido.";
    private static final String INVALID_BODY = "La petición no cumple las reglas de validación.";
    private static final String DUPLICATED_NAME = "Ya existe una sucursal con ese nombre en la franquicia.";

    private final BranchService branches;

    public BranchController(BranchService branches) {
        this.branches = branches;
    }

    @PostMapping("/franchises/{franchiseId}/branches")
    @Operation(summary = "Agregar sucursal", description = "Da de alta una nueva sucursal dentro de la franquicia indicada.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sucursal creada.", content = @Content(schema = @Schema(implementation = BranchResponse.class))),
            @ApiResponse(responseCode = "400", description = INVALID_BODY, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = DUPLICATED_NAME, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<BranchResponse> createBranch(
            @Parameter(description = "Identificador UUID de la franquicia.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID franchiseId,
            @Valid @RequestBody CreateBranchRequest request) {
        BranchResponse response = BranchResponse.from(branches.addBranch(franchiseId, request.name()));
        return ResponseEntity.created(URI.create("/api/v1/franchises/" + franchiseId + "/branches")).body(response);
    }
}