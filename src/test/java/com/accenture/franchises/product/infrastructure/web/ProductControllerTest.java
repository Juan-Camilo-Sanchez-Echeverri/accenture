package com.accenture.franchises.product.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.common.pagination.PageResult;
import com.accenture.franchises.product.application.ProductService;
import com.accenture.franchises.product.domain.Product;
import com.accenture.franchises.product.domain.ProductStock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductService products;

    private final UUID id = UUID.randomUUID();
    private final UUID branchId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-01-01T10:00:00Z");

    private Product product(String name) {
        return Product.restore(id, name, now, now);
    }

    private Product productWithStock() {
        return Product.restore(id, "Hamburguesa", now, now, List.of(new ProductStock(branchId, "Centro", 12)));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(products.create("Hamburguesa")).thenReturn(product("Hamburguesa"));

        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hamburguesa\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/products/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Hamburguesa"));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createRejectsMissingName() throws Exception {
        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsNameTooLong() throws Exception {
        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + "a".repeat(121) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReturns409OnDuplicatedName() throws Exception {
        when(products.create("Hamburguesa")).thenThrow(new DuplicateNameException("Product", "Hamburguesa"));

        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hamburguesa\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate name"));
    }

    @Test
    void listReturnsPageMetadata() throws Exception {
        when(products.findAll(any())).thenReturn(
                PageResult.of(List.of(productWithStock(), product("Pizza")), 1, 2, 5));

        mvc.perform(get("/api/v1/products?page=1&limit=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].name").value("Hamburguesa"))
                .andExpect(jsonPath("$.items[0].stocks[0].branchId").value(branchId.toString()))
                .andExpect(jsonPath("$.items[0].stocks[0].branchName").value("Centro"))
                .andExpect(jsonPath("$.items[0].stocks[0].stock").value(12))
                .andExpect(jsonPath("$.items[1].stocks").isEmpty())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void listAppliesDefaultPagingWhenNoParamsAreGiven() throws Exception {
        when(products.findAll(any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

        mvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void getReturns200() throws Exception {
        when(products.findById(id)).thenReturn(productWithStock());

        mvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Hamburguesa"))
                .andExpect(jsonPath("$.stocks[0].branchName").value("Centro"))
                .andExpect(jsonPath("$.stocks[0].stock").value(12));
    }

    @Test
    void getReturns404WhenMissing() throws Exception {
        when(products.findById(id)).thenThrow(new ResourceNotFoundException("Product", id));

        mvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void getReturns400OnMalformedId() throws Exception {
        mvc.perform(get("/api/v1/products/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void renameReturns200() throws Exception {
        when(products.rename(eq(id), any())).thenReturn(product("Hamburguesa XL"));

        mvc.perform(patch("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hamburguesa XL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hamburguesa XL"));
    }

    @Test
    void renameRejectsBlankName() throws Exception {
        mvc.perform(patch("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void renameReturns404WhenMissing() throws Exception {
        when(products.rename(eq(id), any())).thenThrow(new ResourceNotFoundException("Product", id));

        mvc.perform(patch("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hamburguesa XL\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/v1/products/{id}", id)).andExpect(status().isNoContent());
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Product", id)).when(products).delete(id);

        mvc.perform(delete("/api/v1/products/{id}", id)).andExpect(status().isNotFound());
    }
}