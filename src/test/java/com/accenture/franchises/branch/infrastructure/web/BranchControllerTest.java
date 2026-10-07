package com.accenture.franchises.branch.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accenture.franchises.branch.application.BranchService;
import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.branch.domain.BranchDetail;
import com.accenture.franchises.branch.domain.BranchProduct;
import com.accenture.franchises.branch.domain.BranchProductSummary;
import com.accenture.franchises.common.exception.ConflictException;
import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.common.pagination.PageResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BranchController.class)
class BranchControllerTest {

        @Autowired
        private MockMvc mvc;

        @MockitoBean
        private BranchService branches;

        private final UUID franchiseId = UUID.randomUUID();
        private final UUID branchId = UUID.randomUUID();
        private final UUID productId = UUID.randomUUID();
        private final Instant now = Instant.parse("2026-01-01T10:00:00Z");

        private Branch branch(String name) {
                return Branch.restore(branchId, name, franchiseId, now, now);
        }

        private BranchProduct stock(int quantity) {
                return BranchProduct.restore(branchId, productId, quantity);
        }

        private String productLinkBody(int stock) {
                return "{\"productId\":\"" + productId + "\",\"stock\":" + stock + "}";
        }

        @Test
        void createBranchReturns201WithLocation() throws Exception {
                when(branches.addBranch(franchiseId, "Centro")).thenReturn(branch("Centro"));

                mvc.perform(post("/api/v1/franchises/{franchiseId}/branches", franchiseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Centro\"}"))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location",
                                                "/api/v1/franchises/" + franchiseId + "/branches"))
                                .andExpect(jsonPath("$.id").value(branchId.toString()))
                                .andExpect(jsonPath("$.name").value("Centro"))
                                .andExpect(jsonPath("$.franchiseId").value(franchiseId.toString()));
        }

        @Test
        void createBranchReturns404WhenFranchiseIsMissing() throws Exception {
                when(branches.addBranch(franchiseId, "Centro"))
                                .thenThrow(new ResourceNotFoundException("Franchise", franchiseId));

                mvc.perform(post("/api/v1/franchises/{franchiseId}/branches", franchiseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Centro\"}"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Resource not found"));
        }

        @Test
        void createBranchReturns409OnDuplicatedName() throws Exception {
                when(branches.addBranch(franchiseId, "Centro"))
                                .thenThrow(new DuplicateNameException("Branch", "Centro"));

                mvc.perform(post("/api/v1/franchises/{franchiseId}/branches", franchiseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Centro\"}"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.title").value("Duplicate name"));
        }

        @Test
        void createBranchRejectsBlankName() throws Exception {
                mvc.perform(post("/api/v1/franchises/{franchiseId}/branches", franchiseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"  \"}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors.name").exists());
        }

        @Test
        void createBranchReturns400OnMalformedFranchiseId() throws Exception {
                mvc.perform(post("/api/v1/franchises/not-a-uuid/branches")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Centro\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void listBranchesReturnsPageMetadataWithoutProducts() throws Exception {
                when(branches.findAll(any())).thenReturn(
                                PageResult.of(List.of(branch("Centro"), branch("Norte")), 0, 2, 5));

                mvc.perform(get("/api/v1/branches?page=0&limit=2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.items.length()").value(2))
                                .andExpect(jsonPath("$.items[0].name").value("Centro"))
                                .andExpect(jsonPath("$.items[0].products").doesNotExist())
                                .andExpect(jsonPath("$.page").value(0))
                                .andExpect(jsonPath("$.limit").value(2))
                                .andExpect(jsonPath("$.totalElements").value(5))
                                .andExpect(jsonPath("$.totalPages").value(3));
        }

        @Test
        void listBranchesAppliesDefaultPagingWhenNoParamsAreGiven() throws Exception {
                when(branches.findAll(any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

                mvc.perform(get("/api/v1/branches"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.page").value(0))
                                .andExpect(jsonPath("$.limit").value(20))
                                .andExpect(jsonPath("$.totalElements").value(0))
                                .andExpect(jsonPath("$.totalPages").value(0));
        }

        @Test
        void addProductReturns201WithLocation() throws Exception {
                when(branches.addProduct(branchId, productId, 10)).thenReturn(stock(10));

                mvc.perform(post("/api/v1/branches/{branchId}/products", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productLinkBody(10)))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location",
                                                "/api/v1/branches/" + branchId + "/products/" + productId))
                                .andExpect(jsonPath("$.branchId").value(branchId.toString()))
                                .andExpect(jsonPath("$.productId").value(productId.toString()))
                                .andExpect(jsonPath("$.stock").value(10));
        }

        @Test
        void addProductReturns404WhenBranchIsMissing() throws Exception {
                when(branches.addProduct(branchId, productId, 10))
                                .thenThrow(new ResourceNotFoundException("Branch", branchId));

                mvc.perform(post("/api/v1/branches/{branchId}/products", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productLinkBody(10)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Resource not found"));
        }

        @Test
        void addProductReturns409WhenAlreadyLinked() throws Exception {
                when(branches.addProduct(branchId, productId, 10))
                                .thenThrow(new ConflictException(
                                                "Product " + productId + " is already added to branch " + branchId));

                mvc.perform(post("/api/v1/branches/{branchId}/products", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productLinkBody(10)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.title").value("Conflict"));
        }

        @Test
        void addProductRejectsNegativeStock() throws Exception {
                mvc.perform(post("/api/v1/branches/{branchId}/products", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"productId\":\"" + productId + "\",\"stock\":-1}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void addProductReturns400OnMalformedBranchId() throws Exception {
                mvc.perform(post("/api/v1/branches/not-a-uuid/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productLinkBody(10)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void removeProductReturns204() throws Exception {
                mvc.perform(delete("/api/v1/branches/{branchId}/products/{productId}", branchId, productId))
                                .andExpect(status().isNoContent());
        }

        @Test
        void removeProductReturns404WhenBranchIsMissing() throws Exception {
                doThrow(new ResourceNotFoundException("Branch", branchId)).when(branches).removeProduct(branchId,
                                productId);

                mvc.perform(delete("/api/v1/branches/{branchId}/products/{productId}", branchId, productId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Resource not found"));
        }

        @Test
        void removeProductReturns404WhenProductIsNotLinked() throws Exception {
                doThrow(new ResourceNotFoundException("BranchProduct", branchId + "/" + productId))
                                .when(branches).removeProduct(branchId, productId);

                mvc.perform(delete("/api/v1/branches/{branchId}/products/{productId}", branchId, productId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Resource not found"));
        }

        @Test
        void removeProductReturns400OnMalformedProductId() throws Exception {
                mvc.perform(delete("/api/v1/branches/{branchId}/products/not-a-uuid", branchId))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void updateStockReturns200WithNewQuantity() throws Exception {
                when(branches.updateStock(branchId, productId, 25)).thenReturn(stock(25));

                mvc.perform(patch("/api/v1/branches/{branchId}/products/{productId}", branchId, productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"stock\":25}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.branchId").value(branchId.toString()))
                                .andExpect(jsonPath("$.productId").value(productId.toString()))
                                .andExpect(jsonPath("$.stock").value(25));
        }

        @Test
        void updateStockRejectsNegativeStock() throws Exception {
                mvc.perform(patch("/api/v1/branches/{branchId}/products/{productId}", branchId, productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"stock\":-1}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void updateStockRejectsMissingStock() throws Exception {
                mvc.perform(patch("/api/v1/branches/{branchId}/products/{productId}", branchId, productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void updateStockReturns404WhenBranchIsMissing() throws Exception {
                when(branches.updateStock(branchId, productId, 25))
                                .thenThrow(new ResourceNotFoundException("Branch", branchId));

                mvc.perform(patch("/api/v1/branches/{branchId}/products/{productId}", branchId, productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"stock\":25}"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Resource not found"));
        }

        @Test
        void updateStockReturns404WhenProductIsNotLinked() throws Exception {
                when(branches.updateStock(branchId, productId, 25))
                                .thenThrow(new ResourceNotFoundException("BranchProduct", branchId + "/" + productId));

                mvc.perform(patch("/api/v1/branches/{branchId}/products/{productId}", branchId, productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"stock\":25}"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Resource not found"));
        }

        @Test
        void updateStockReturns400OnMalformedProductId() throws Exception {
                mvc.perform(patch("/api/v1/branches/{branchId}/products/not-a-uuid", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"stock\":25}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getBranchReturns200WithItsProducts() throws Exception {
                BranchDetail detail = new BranchDetail(
                                branch("Centro"),
                                List.of(new BranchProductSummary(productId, "Hamburguesa", 15)));
                when(branches.findDetail(branchId)).thenReturn(detail);

                mvc.perform(get("/api/v1/branches/{branchId}", branchId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(branchId.toString()))
                                .andExpect(jsonPath("$.name").value("Centro"))
                                .andExpect(jsonPath("$.franchiseId").value(franchiseId.toString()))
                                .andExpect(jsonPath("$.products[0].productName").value("Hamburguesa"))
                                .andExpect(jsonPath("$.products[0].stock").value(15))
                                .andExpect(jsonPath("$.products.length()").value(1));
        }

        @Test
        void getBranchReturns404WhenBranchIsMissing() throws Exception {
                when(branches.findDetail(branchId))
                                .thenThrow(new ResourceNotFoundException("Branch", branchId));

                mvc.perform(get("/api/v1/branches/{branchId}", branchId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Resource not found"));
        }

        @Test
        void getBranchReturns400OnMalformedBranchId() throws Exception {
                mvc.perform(get("/api/v1/branches/not-a-uuid"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void renameBranchReturns200WithTheNewName() throws Exception {
                when(branches.rename(branchId, "Norte")).thenReturn(branch("Norte"));

                mvc.perform(patch("/api/v1/branches/{branchId}", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Norte\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Norte"));
        }

        @Test
        void renameBranchReturns409OnDuplicatedName() throws Exception {
                when(branches.rename(branchId, "Norte"))
                                .thenThrow(new DuplicateNameException("Branch", "Norte"));

                mvc.perform(patch("/api/v1/branches/{branchId}", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Norte\"}"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.title").value("Duplicate name"));
        }

        @Test
        void renameBranchReturns404WhenBranchIsMissing() throws Exception {
                when(branches.rename(branchId, "Norte"))
                                .thenThrow(new ResourceNotFoundException("Branch", branchId));

                mvc.perform(patch("/api/v1/branches/{branchId}", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Norte\"}"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Resource not found"));
        }

        @Test
        void renameBranchRejectsBlankName() throws Exception {
                mvc.perform(patch("/api/v1/branches/{branchId}", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"  \"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void deleteBranchReturns204() throws Exception {
                mvc.perform(delete("/api/v1/branches/{branchId}", branchId))
                                .andExpect(status().isNoContent());
        }

        @Test
        void deleteBranchReturns404WhenBranchIsMissing() throws Exception {
                doThrow(new ResourceNotFoundException("Branch", branchId)).when(branches).delete(branchId);

                mvc.perform(delete("/api/v1/branches/{branchId}", branchId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Resource not found"));
        }

        @Test
        void deleteBranchReturns400OnMalformedBranchId() throws Exception {
                mvc.perform(delete("/api/v1/branches/not-a-uuid"))
                                .andExpect(status().isBadRequest());
        }
}