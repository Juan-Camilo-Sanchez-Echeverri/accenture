package com.accenture.franchises.branch.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.franchise.infrastructure.persistence.FranchiseJpaEntity;
import com.accenture.franchises.franchise.infrastructure.persistence.FranchiseJpaRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BranchRepositoryAdapter.class)
class BranchRepositoryAdapterTest {

    @Autowired
    private BranchRepositoryAdapter adapter;

    @Autowired
    private BranchJpaRepository branchRepository;

    @Autowired
    private FranchiseJpaRepository franchiseRepository;

    private UUID franchiseId;

    @BeforeEach
    void setUp() {
        franchiseId = franchiseRepository.saveAndFlush(new FranchiseJpaEntity("Acme")).getId();
    }

    @Test
    void saveAssignsIdAndTimestampsAndKeepsTheFranchise() {
        Branch saved = adapter.save(Branch.create("Centro", franchiseId));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getFranchiseId()).isEqualTo(franchiseId);
        assertThat(branchRepository.findById(saved.getId())).isPresent();
        assertThat(adapter.existsByNameInFranchise("Centro", franchiseId)).isTrue();
    }

    @Test
    void databaseRejectsDuplicatedBranchNameInSameFranchise() {
        adapter.save(Branch.create("Centro", franchiseId));

        assertThatThrownBy(() -> adapter.save(Branch.create("Centro", franchiseId)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void detectsExistingNameIgnoringCaseScopedToFranchise() {
        adapter.save(Branch.create("Centro", franchiseId));

        assertThat(adapter.existsByNameInFranchise("centro", franchiseId)).isTrue();
        assertThat(adapter.existsByNameInFranchise("Centro", UUID.randomUUID())).isFalse();
        assertThat(adapter.existsByNameInFranchise("Otro", franchiseId)).isFalse();
    }

    @Test
    void allowsTheSameBranchNameInDifferentFranchises() {
        UUID other = franchiseRepository.saveAndFlush(new FranchiseJpaEntity("Beta")).getId();

        adapter.save(Branch.create("Centro", franchiseId));
        adapter.save(Branch.create("Centro", other));

        assertThat(branchRepository.count()).isEqualTo(2);
    }

    @Test
    void deleteFranchiseCascadesToItsBranches() {
        UUID franchiseToDelete = franchiseRepository.saveAndFlush(new FranchiseJpaEntity("Adios")).getId();
        adapter.save(Branch.create("Centro", franchiseToDelete));
        adapter.save(Branch.create("Norte", franchiseToDelete));

        franchiseRepository.deleteById(franchiseToDelete);
        franchiseRepository.flush();

        assertThat(branchRepository.count()).isZero();
    }
}