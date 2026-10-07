package com.accenture.franchises.franchise.infrastructure.web;

import com.accenture.franchises.franchise.application.FranchiseService;
import com.accenture.franchises.franchise.domain.Franchise;
import com.accenture.franchises.franchise.infrastructure.web.dto.CreateFranchiseRequest;
import com.accenture.franchises.franchise.infrastructure.web.dto.FranchiseResponse;
import com.accenture.franchises.franchise.infrastructure.web.dto.RenameFranchiseRequest;
import com.accenture.franchises.common.infrastructure.web.dto.PageResponse;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/franchises")
@Tag(name = "Franquicias", description = "Alta, consulta, renombrado y baja de franquicias.")
public class FranchiseController {

    private static final String ID_PARAMETER = "Identificador UUID de la franquicia.";

    private static final String NOT_FOUND = "No existe ninguna franquicia con ese identificador.";
    private static final String INVALID_ID = "El identificador no es un UUID válido.";
    private static final String INVALID_BODY = "La petición no cumple las reglas de validación.";
    private static final String DUPLICATED_NAME = "Ya existe una franquicia con ese nombre.";

    private final FranchiseService franchises;

    public FranchiseController(FranchiseService franchises) {
        this.franchises = franchises;
    }

    @PostMapping
    @Operation(summary = "Crear franquicia", description = "Da de alta una franquicia con un nombre propio.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Franquicia creada.", content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
            @ApiResponse(responseCode = "400", description = INVALID_BODY, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = DUPLICATED_NAME, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<FranchiseResponse> create(@Valid @RequestBody CreateFranchiseRequest request) {
        FranchiseResponse response = FranchiseResponse.from(franchises.create(request.name()));
        return ResponseEntity.created(URI.create("/api/v1/franchises/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar franquicias", description = "Devuelve una página de franquicias ordenadas por fecha de creación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de franquicias.", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    public PageResponse<FranchiseResponse> list(
            @RequestParam(defaultValue = "0") @Parameter(description = "Página a consultar, empezando en 0.", example = "0") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Elementos por página, con un máximo de 100.", example = "20") int limit) {
        PageResult<Franchise> result = franchises.findAll(PageQuery.of(page, limit));
        return PageResponse.from(result, FranchiseResponse::from);
    }

    @GetMapping("/{franchiseId}")
    @Operation(summary = "Obtener una franquicia", description = "Devuelve la franquicia indicada por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Franquicia encontrada.", content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
            @ApiResponse(responseCode = "400", description = INVALID_ID, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public FranchiseResponse get(
            @Parameter(description = ID_PARAMETER, example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID franchiseId) {
        return FranchiseResponse.from(franchises.findById(franchiseId));
    }

    @PatchMapping("/{franchiseId}")
    @Operation(summary = "Renombrar franquicia", description = "Cambia el nombre de una franquicia existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Franquicia renombrada.", content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
            @ApiResponse(responseCode = "400", description = INVALID_BODY, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = DUPLICATED_NAME, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public FranchiseResponse rename(
            @Parameter(description = ID_PARAMETER, example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID franchiseId,
            @Valid @RequestBody RenameFranchiseRequest request) {
        return FranchiseResponse.from(franchises.rename(franchiseId, request.name()));
    }

    @DeleteMapping("/{franchiseId}")
    @Operation(summary = "Eliminar franquicia", description = "Da de baja una franquicia y todo lo asociado a ella.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Franquicia eliminada."),
            @ApiResponse(responseCode = "400", description = INVALID_ID, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = ID_PARAMETER, example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID franchiseId) {
        franchises.delete(franchiseId);
        return ResponseEntity.noContent().build();
    }
}
