package com.accenture.franchises.franchise.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accenture.franchises.franchise.domain.Franchise;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(FranchiseRepositoryAdapter.class)
class FranchiseRepositoryAdapterTest {

    @Autowired
    private FranchiseRepositoryAdapter adapter;

    @Autowired
    private FranchiseJpaRepository repository;

    @Test
    void saveAssignsIdAndTimestamps() {
        Franchise saved = adapter.save(Franchise.create("Acme"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void databaseRejectsDuplicatedName() {
        adapter.save(Franchise.create("Acme"));

        assertThatThrownBy(() -> adapter.save(Franchise.create("Acme")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void detectsExistingNameIgnoringCase() {
        adapter.save(Franchise.create("Acme"));

        assertThat(adapter.existsByNameIgnoreCase("acme")).isTrue();
        assertThat(adapter.existsByNameIgnoreCase("ACME")).isTrue();
        assertThat(adapter.existsByNameIgnoreCase("Other")).isFalse();
    }

    @Test
    void findAllReturnsAStablePage() {
        for (int i = 1; i <= 5; i++) {
            adapter.save(Franchise.create("Franchise %02d".formatted(i)));
        }

        PageResult<Franchise> page = adapter.findAll(PageQuery.of(1, 2));

        assertThat(page.items()).hasSize(2);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.limit()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.totalPages()).isEqualTo(3);
    }

    @Test
    void renameKeepsTheRowAndRefreshesTheTimestamp() {
        Franchise saved = adapter.save(Franchise.create("Acme"));
        adapter.save(saved.rename("Acme Corp"));

        Franchise reloaded = adapter.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Acme Corp");
        assertThat(reloaded.getCreatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void deleteRemovesTheRow() {
        Franchise saved = adapter.save(Franchise.create("Acme"));

        adapter.delete(saved.getId());

        assertThat(adapter.findById(saved.getId())).isEmpty();
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
    }
}
