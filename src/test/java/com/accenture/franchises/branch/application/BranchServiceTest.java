package com.accenture.franchises.branch.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.branch.domain.BranchRepositoryPort;
import com.accenture.franchises.franchise.domain.Franchise;
import com.accenture.franchises.franchise.domain.FranchiseRepositoryPort;
import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepositoryPort branches;

    @Mock
    private FranchiseRepositoryPort franchises;

    @InjectMocks
    private BranchService service;

    private final UUID franchiseId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-01-01T10:00:00Z");

    private void existingFranchise() {
        when(franchises.findById(franchiseId))
                .thenReturn(Optional.of(Franchise.restore(franchiseId, "Acme", now, now)));
    }

    @Test
    void addBranchSavesUnderTheFranchise() {
        existingFranchise();
        when(branches.existsByNameInFranchise("Centro", franchiseId)).thenReturn(false);
        when(branches.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Branch created = service.addBranch(franchiseId, "Centro");

        assertThat(created.getName()).isEqualTo("Centro");
        assertThat(created.getFranchiseId()).isEqualTo(franchiseId);
        assertThat(created.getId()).isNull();
    }

    @Test
    void addBranchFailsWhenFranchiseIsMissing() {
        when(franchises.findById(franchiseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addBranch(franchiseId, "Centro"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).save(any());
    }

    @Test
    void addBranchRejectsDuplicatedNameInFranchise() {
        existingFranchise();
        when(branches.existsByNameInFranchise("Centro", franchiseId)).thenReturn(true);

        assertThatThrownBy(() -> service.addBranch(franchiseId, "Centro"))
                .isInstanceOf(DuplicateNameException.class);

        verify(branches, never()).save(any());
    }
}