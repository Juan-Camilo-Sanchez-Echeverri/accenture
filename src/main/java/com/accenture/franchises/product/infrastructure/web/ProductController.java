package com.accenture.franchises.product.infrastructure.web;

import com.accenture.franchises.common.infrastructure.web.dto.PageResponse;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import com.accenture.franchises.product.application.ProductService;
import com.accenture.franchises.product.domain.Product;
import com.accenture.franchises.product.infrastructure.web.dto.CreateProductRequest;
import com.accenture.franchises.product.infrastructure.web.dto.ProductResponse;
import com.accenture.franchises.product.infrastructure.web.dto.RenameProductRequest;
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
@RequestMapping("/api/v1/products")
@Tag(name = "Productos", description = "Catálogo de productos disponibles para las sucursales.")
public class ProductController {

    private static final String ID_PARAMETER = "Identificador UUID del producto.";

    private static final String NOT_FOUND = "No existe ningún producto con ese identificador.";
    private static final String INVALID_ID = "El identificador no es un UUID válido.";
    private static final String INVALID_BODY = "La petición no cumple las reglas de validación.";
    private static final String DUPLICATED_NAME = "Ya existe un producto con ese nombre.";

    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @PostMapping
    @Operation(summary = "Crear producto", description = "Da de alta un producto en el catálogo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado.", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = INVALID_BODY, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = DUPLICATED_NAME, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = ProductResponse.from(products.create(request.name()));
        return ResponseEntity.created(URI.create("/api/v1/products/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Devuelve una página de productos con su stock por sucursal, ordenados por fecha de creación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de productos.", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    public PageResponse<ProductResponse> list(
            @RequestParam(defaultValue = "0") @Parameter(description = "Página a consultar, empezando en 0.", example = "0") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Elementos por página, con un máximo de 100.", example = "20") int limit) {
        PageResult<Product> result = products.findAll(PageQuery.of(page, limit));
        return PageResponse.from(result, ProductResponse::from);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Obtener un producto", description = "Devuelve el producto indicado con su stock por sucursal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado.", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = INVALID_ID, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ProductResponse get(
            @Parameter(description = ID_PARAMETER, example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID productId) {
        return ProductResponse.from(products.findById(productId));
    }

    @PatchMapping("/{productId}")
    @Operation(summary = "Renombrar producto", description = "Cambia el nombre de un producto existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto renombrado.", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = INVALID_BODY, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = DUPLICATED_NAME, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ProductResponse rename(
            @Parameter(description = ID_PARAMETER, example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID productId,
            @Valid @RequestBody RenameProductRequest request) {
        return ProductResponse.from(products.rename(productId, request.name()));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Eliminar producto", description = "Da de baja un producto del catálogo y lo quita de todas las sucursales.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado."),
            @ApiResponse(responseCode = "400", description = INVALID_ID, content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = ID_PARAMETER, example = "0f8d1b6e-2e2f-4d3e-9b7a-1a2b3c4d5e6f") @PathVariable UUID productId) {
        products.delete(productId);
        return ResponseEntity.noContent().build();
    }
}