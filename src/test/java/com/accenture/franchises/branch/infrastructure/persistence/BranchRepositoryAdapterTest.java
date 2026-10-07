package com.accenture.franchises.branch.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import com.accenture.franchises.franchise.infrastructure.persistence.FranchiseJpaEntity;
import com.accenture.franchises.franchise.infrastructure.persistence.FranchiseJpaRepository;
import com.accenture.franchises.product.infrastructure.persistence.ProductJpaEntity;
import com.accenture.franchises.product.infrastructure.persistence.ProductJpaRepository;
import java.util.NoSuchElementException;
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
    private BranchProductJpaRepository stockRepository;

    @Autowired
    private FranchiseJpaRepository franchiseRepository;

    @Autowired
    private ProductJpaRepository productRepository;

    private UUID franchiseId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        franchiseId = franchiseRepository.saveAndFlush(new FranchiseJpaEntity("Acme")).getId();
        productId = productRepository.saveAndFlush(new ProductJpaEntity("Hamburguesa")).getId();
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

    @Test
    void addProductPersistsTheStock() {
        UUID branchId = adapter.save(Branch.create("Centro", franchiseId)).getId();

        adapter.addProduct(branchId, productId, 12);

        assertThat(stockRepository.count()).isEqualTo(1);
        Branch reloaded = adapter.findById(branchId).orElseThrow();
        assertThat(adapter.findProductStock(reloaded.getId(), productId).orElseThrow().getStock()).isEqualTo(12);
    }

    @Test
    void databaseRejectsDuplicatedProductInSameBranch() {
        UUID branchId = adapter.save(Branch.create("Centro", franchiseId)).getId();
        adapter.addProduct(branchId, productId, 5);

        assertThatThrownBy(() -> adapter.addProduct(branchId, productId, 9))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findProductStockIsEmptyWhenTheLinkDoesNotExist() {
        assertThat(adapter.findProductStock(UUID.randomUUID(), productId)).isEmpty();
    }

    @Test
    void removeProductDeletesTheLinkAndItsStock() {
        UUID branchId = adapter.save(Branch.create("Centro", franchiseId)).getId();
        adapter.addProduct(branchId, productId, 7);

        adapter.removeProduct(branchId, productId);

        assertThat(stockRepository.count()).isZero();
        assertThat(adapter.findProductStock(branchId, productId)).isEmpty();
    }

    @Test
    void removeProductIsNoOpWhenTheLinkDoesNotExist() {
        adapter.removeProduct(UUID.randomUUID(), productId);

        assertThat(stockRepository.count()).isZero();
    }

    @Test
    void updateStockChangesTheQuantityInTheStore() {
        UUID branchId = adapter.save(Branch.create("Centro", franchiseId)).getId();
        adapter.addProduct(branchId, productId, 7);

        adapter.updateStock(branchId, productId, 20);

        assertThat(stockRepository.count()).isEqualTo(1);
        assertThat(adapter.findProductStock(branchId, productId).orElseThrow().getStock()).isEqualTo(20);
    }

    @Test
    void updateStockFailsWhenTheLinkDoesNotExist() {
        assertThatThrownBy(() -> adapter.updateStock(UUID.randomUUID(), productId, 20))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findAllReturnsAPaginatedPageOfAllBranches() {
        UUID other = franchiseRepository.saveAndFlush(new FranchiseJpaEntity("Beta")).getId();
        adapter.save(Branch.create("Centro", franchiseId));
        adapter.save(Branch.create("Norte", franchiseId));
        adapter.save(Branch.create("Alfa", other));

        PageResult<Branch> page = adapter.findAll(PageQuery.of(0, 2));

        assertThat(page.items()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(2);
    }
}