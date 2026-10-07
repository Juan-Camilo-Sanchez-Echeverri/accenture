package com.accenture.franchises.branch.infrastructure.web;

import com.accenture.franchises.branch.application.BranchService;
import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.branch.infrastructure.web.dto.BranchDetailResponse;
import com.accenture.franchises.branch.infrastructure.web.dto.BranchResponse;
import com.accenture.franchises.branch.infrastructure.web.dto.CreateBranchProductRequest;
import com.accenture.franchises.branch.infrastructure.web.dto.CreateBranchRequest;
import com.accenture.franchises.branch.infrastructure.web.dto.ProductStockResponse;
import com.accenture.franchises.branch.infrastructure.web.dto.RenameBranchRequest;
import com.accenture.franchises.branch.infrastructure.web.dto.UpdateStockRequest;
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
@RequestMapping("/api/v1")
@Tag(name = "Sucursales", description = "Sucursales de una franquicia.")
public class BranchController {

        private static final String NOT_FOUND = "No existe la franquicia, la sucursal, el producto o el vínculo indicado.";
        private static final String INVALID_ID = "El identificador no es un UUID válido.";
        private static final String INVALID_BODY = "La petición no cumple las reglas de validación.";
        private static final String DUPLICATED_NAME = "Ya existe una sucursal con ese nombre en la franquicia.";
        private static final String PRODUCT_ALREADY_LINKED = "El producto ya está vinculado a la sucursal.";

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
                return ResponseEntity.created(URI.create("/api/v1/franchises/" + franchiseId + "/branches"))
                                .body(response);
        }

        @GetMapping("/branches")
        @Operation(summary = "Listar sucursales", description = "Devuelve una página de sucursales, sin incluir sus productos.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Página de sucursales.", content = @Content(schema = @Schema(implementation = PageResponse.class)))
        })
        public PageResponse<BranchResponse> listBranches(
                        @RequestParam(defaultValue = "0") @Parameter(description = "Página a consultar, empezando en 0.", example = "0") int page,
                        @RequestParam(defaultValue = "20") @Parameter(description = "Elementos por página, con un máximo de 100.", example = "20") int limit) {
                PageResult<Branch> result = branches.findAll(PageQuery.of(page, limit));
                return PageResponse.from(result, BranchResponse::from);
        }

        @GetMapping("/branches/{branchId}")
        @Operation(summary = "Obtener una sucursal", description = "Devuelve la sucursal indicada por su identificador, con el listado de los productos que oferta.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Sucursal encontrada.", content = @Content(schema = @Schema(implementation = BranchDetailResponse.class))),
                        @ApiResponse(responseCode = "400", description = INVALID_ID, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        public BranchDetailResponse getBranch(
                        @Parameter(description = "Identificador UUID de la sucursal.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID branchId) {
                return BranchDetailResponse.from(branches.findDetail(branchId));
        }

        @PatchMapping("/branches/{branchId}")
        @Operation(summary = "Renombrar sucursal", description = "Cambia el nombre de una sucursal existente.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Sucursal renombrada.", content = @Content(schema = @Schema(implementation = BranchResponse.class))),
                        @ApiResponse(responseCode = "400", description = INVALID_BODY, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "409", description = DUPLICATED_NAME, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        public BranchResponse renameBranch(
                        @Parameter(description = "Identificador UUID de la sucursal.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID branchId,
                        @Valid @RequestBody RenameBranchRequest request) {
                return BranchResponse.from(branches.rename(branchId, request.name()));
        }

        @DeleteMapping("/branches/{branchId}")
        @Operation(summary = "Eliminar sucursal", description = "Da de baja la sucursal y el stock de sus productos en ella.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Sucursal eliminada."),
                        @ApiResponse(responseCode = "400", description = INVALID_ID, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        public ResponseEntity<Void> deleteBranch(
                        @Parameter(description = "Identificador UUID de la sucursal.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID branchId) {
                branches.delete(branchId);
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/branches/{branchId}/products")
        @Operation(summary = "Agregar producto a sucursal", description = "Vincula un producto del catálogo a la sucursal con su stock inicial.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Producto vinculado a la sucursal.", content = @Content(schema = @Schema(implementation = ProductStockResponse.class))),
                        @ApiResponse(responseCode = "400", description = INVALID_BODY, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "409", description = PRODUCT_ALREADY_LINKED, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        public ResponseEntity<ProductStockResponse> addProduct(
                        @Parameter(description = "Identificador UUID de la sucursal.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID branchId,
                        @Valid @RequestBody CreateBranchProductRequest request) {
                ProductStockResponse response = ProductStockResponse.from(
                                branches.addProduct(branchId, request.productId(), request.stock()));
                return ResponseEntity
                                .created(URI.create(
                                                "/api/v1/branches/" + branchId + "/products/" + response.productId()))
                                .body(response);
        }

        @PatchMapping("/branches/{branchId}/products/{productId}")
        @Operation(summary = "Cambiar stock de un producto en sucursal", description = "Modifica la cantidad en stock que el producto tiene en la sucursal.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Stock actualizado.", content = @Content(schema = @Schema(implementation = ProductStockResponse.class))),
                        @ApiResponse(responseCode = "400", description = INVALID_BODY + " "
                                        + INVALID_ID, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        public ResponseEntity<ProductStockResponse> updateStock(
                        @Parameter(description = "Identificador UUID de la sucursal.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID branchId,
                        @Parameter(description = "Identificador UUID del producto.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID productId,
                        @Valid @RequestBody UpdateStockRequest request) {
                ProductStockResponse response = ProductStockResponse.from(
                                branches.updateStock(branchId, productId, request.stock()));
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/branches/{branchId}/products/{productId}")
        @Operation(summary = "Quitar producto de sucursal", description = "Desvincula un producto de la sucursal; se descarta el stock que tenía en ella.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Producto desvinculado de la sucursal."),
                        @ApiResponse(responseCode = "400", description = INVALID_ID, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        public ResponseEntity<Void> removeProduct(
                        @Parameter(description = "Identificador UUID de la sucursal.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID branchId,
                        @Parameter(description = "Identificador UUID del producto.", example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID productId) {

                branches.removeProduct(branchId, productId);
                return ResponseEntity.noContent().build();
        }
}