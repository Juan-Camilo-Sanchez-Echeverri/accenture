package com.accenture.franchises.franchise.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accenture.franchises.franchise.domain.Franchise;
import com.accenture.franchises.franchise.domain.FranchiseRepositoryPort;
import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceTest {

    @Mock
    private FranchiseRepositoryPort franchises;

    @InjectMocks
    private FranchiseService service;

    private final UUID id = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void createSavesTheFranchise() {
        when(franchises.existsByNameIgnoreCase("Acme")).thenReturn(false);
        when(franchises.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Franchise created = service.create("Acme");

        ArgumentCaptor<Franchise> captor = ArgumentCaptor.forClass(Franchise.class);
        verify(franchises).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Acme");
        assertThat(captor.getValue().getId()).isNull();
        assertThat(created.getName()).isEqualTo("Acme");
    }

    @Test
    void createRejectsDuplicatedNameIgnoringCase() {
        when(franchises.existsByNameIgnoreCase("acme")).thenReturn(true);

        assertThatThrownBy(() -> service.create("acme")).isInstanceOf(DuplicateNameException.class);

        verify(franchises, never()).save(any());
    }

    @Test
    void findAllDelegatesTheQueryToThePort() {
        PageQuery query = PageQuery.of(1, 10);
        PageResult<Franchise> expected = PageResult.of(List.of(), 1, 10, 0);
        when(franchises.findAll(query)).thenReturn(expected);

        assertThat(service.findAll(query)).isSameAs(expected);
    }

    @Test
    void findByIdReturnsTheFranchise() {
        Franchise franchise = Franchise.restore(id, "Acme", now, now);
        when(franchises.findById(id)).thenReturn(Optional.of(franchise));

        assertThat(service.findById(id)).isSameAs(franchise);
    }

    @Test
    void findByIdFailsWhenMissing() {
        when(franchises.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void renameReturnsTheUpdatedFranchise() {
        Franchise franchise = Franchise.restore(id, "Acme", now, now);
        when(franchises.findById(id)).thenReturn(Optional.of(franchise));
        when(franchises.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Franchise renamed = service.rename(id, "Acme Corp");

        assertThat(renamed.getName()).isEqualTo("Acme Corp");
        assertThat(renamed.getId()).isEqualTo(id);
        assertThat(renamed.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void renameKeepsTheSameNameWhenOnlyCaseChanges() {
        when(franchises.findById(id)).thenReturn(Optional.of(Franchise.restore(id, "Acme", now, now)));
        when(franchises.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Franchise renamed = service.rename(id, "ACME");

        assertThat(renamed.getName()).isEqualTo("ACME");
        verify(franchises, never()).existsByNameIgnoreCase(any());
    }

    @Test
    void renameRejectsNameTakenByAnotherFranchise() {
        when(franchises.findById(id)).thenReturn(Optional.of(Franchise.restore(id, "Acme", now, now)));
        when(franchises.existsByNameIgnoreCase("Other")).thenReturn(true);

        assertThatThrownBy(() -> service.rename(id, "Other"))
                .isInstanceOf(DuplicateNameException.class);

        verify(franchises, never()).save(any());
    }

    @Test
    void deleteRemovesTheFranchise() {
        when(franchises.findById(id)).thenReturn(Optional.of(Franchise.restore(id, "Acme", now, now)));

        service.delete(id);

        verify(franchises).delete(id);
    }

    @Test
    void deleteFailsWhenMissing() {
        when(franchises.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(ResourceNotFoundException.class);

        verify(franchises, never()).delete(any());
    }
}
