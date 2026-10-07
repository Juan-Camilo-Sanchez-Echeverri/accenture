package com.accenture.franchises.branch.infrastructure.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accenture.franchises.branch.application.BranchService;
import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import java.time.Instant;
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
    private final Instant now = Instant.parse("2026-01-01T10:00:00Z");

    private Branch branch(String name) {
        return Branch.restore(branchId, name, franchiseId, now, now);
    }

    @Test
    void createBranchReturns201WithLocation() throws Exception {
        when(branches.addBranch(franchiseId, "Centro")).thenReturn(branch("Centro"));

        mvc.perform(post("/api/v1/franchises/{franchiseId}/branches", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Centro\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/franchises/" + franchiseId + "/branches"))
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
}