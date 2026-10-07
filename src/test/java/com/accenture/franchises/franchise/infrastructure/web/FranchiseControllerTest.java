package com.accenture.franchises.franchise.infrastructure.web;

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

import com.accenture.franchises.branch.application.BranchService;
import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.franchise.application.FranchiseService;
import com.accenture.franchises.franchise.domain.Franchise;
import com.accenture.franchises.franchise.domain.TopProduct;
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

@WebMvcTest(FranchiseController.class)
class FranchiseControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FranchiseService franchises;

    @MockitoBean
    private BranchService branches;

    private final UUID id = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-01-01T10:00:00Z");

    private Franchise franchise(String name) {
        return Franchise.restore(id, name, now, now);
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(franchises.create("Acme")).thenReturn(franchise("Acme"));

        mvc.perform(post("/api/v1/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/franchises/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.version").doesNotExist());
    }

    @Test
    void createRejectsBlankName() throws Exception {
        mvc.perform(post("/api/v1/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createRejectsMissingName() throws Exception {
        mvc.perform(post("/api/v1/franchises").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsNameTooLong() throws Exception {
        mvc.perform(post("/api/v1/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + "a".repeat(121) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReturns409OnDuplicatedName() throws Exception {
        when(franchises.create("Acme")).thenThrow(new DuplicateNameException("Franchise", "Acme"));

        mvc.perform(post("/api/v1/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate name"));
    }

    @Test
    void listReturnsPageMetadata() throws Exception {
        when(franchises.findAll(any())).thenReturn(
                PageResult.of(List.of(franchise("Acme"), franchise("Beta")), 1, 2, 5));

        mvc.perform(get("/api/v1/franchises?page=1&limit=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].name").value("Acme"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void listAppliesDefaultPagingWhenNoParamsAreGiven() throws Exception {
        when(franchises.findAll(any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

        mvc.perform(get("/api/v1/franchises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void getReturns200WithTheBranchesOfTheFranchise() throws Exception {
        when(franchises.findById(id)).thenReturn(franchise("Acme"));
        when(branches.findAllByFranchise(id))
                .thenReturn(List.of(Branch.restore(UUID.randomUUID(), "Centro", id, now, now)));

        mvc.perform(get("/api/v1/franchises/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.branches[0].name").value("Centro"))
                .andExpect(jsonPath("$.branches.length()").value(1));
    }

    @Test
    void getReturns404WhenMissing() throws Exception {
        when(franchises.findById(id)).thenThrow(new ResourceNotFoundException("Franchise", id));

        mvc.perform(get("/api/v1/franchises/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void getReturns400OnMalformedId() throws Exception {
        mvc.perform(get("/api/v1/franchises/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void renameReturns200() throws Exception {
        when(franchises.rename(eq(id), any())).thenReturn(franchise("Acme Corp"));

        mvc.perform(patch("/api/v1/franchises/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme Corp\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Corp"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/v1/franchises/{id}", id)).andExpect(status().isNoContent());
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Franchise", id)).when(franchises).delete(id);

        mvc.perform(delete("/api/v1/franchises/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void topProductsReturns200WithOneProductPerBranch() throws Exception {
        when(franchises.topProductsPerBranch(id)).thenReturn(List.of(
                new TopProduct(UUID.randomUUID(), "Centro", UUID.randomUUID(), "Hamburguesa", 40)));

        mvc.perform(get("/api/v1/franchises/{id}/top-products", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].branchName").value("Centro"))
                .andExpect(jsonPath("$[0].productName").value("Hamburguesa"))
                .andExpect(jsonPath("$[0].stock").value(40));
    }

    @Test
    void topProductsReturns404WhenFranchiseIsMissing() throws Exception {
        when(franchises.topProductsPerBranch(id))
                .thenThrow(new ResourceNotFoundException("Franchise", id));

        mvc.perform(get("/api/v1/franchises/{id}/top-products", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void topProductsReturns400OnMalformedId() throws Exception {
        mvc.perform(get("/api/v1/franchises/not-a-uuid/top-products"))
                .andExpect(status().isBadRequest());
    }
}
