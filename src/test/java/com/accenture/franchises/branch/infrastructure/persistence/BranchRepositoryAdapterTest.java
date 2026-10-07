package com.accenture.franchises.branch.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.branch.domain.TopProduct;
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
    void topProductsPerBranchReturnsTheBestProductOfEachBranch() {
        UUID product1 = productRepository.saveAndFlush(new ProductJpaEntity("Pizza")).getId();
        UUID branchA = adapter.save(Branch.create("North", franchiseId)).getId();
        UUID branchB = adapter.save(Branch.create("South", franchiseId)).getId();
        adapter.addProduct(branchA, productId, 5);
        adapter.addProduct(branchA, product1, 20);
        adapter.addProduct(branchB, productId, 30);

        var top = adapter.findTopProductsPerBranch(franchiseId);

        assertThat(top).extracting(TopProduct::branchName).containsExactly("North", "South");
        assertThat(top).extracting(TopProduct::productId)
                .containsExactly(product1, productId);
        assertThat(top).extracting(TopProduct::stock).containsExactly(20, 30);
        assertThat(top.get(0).productName()).isEqualTo("Pizza");
    }

    @Test
    void topProductsPerBranchIsEmptyWhenTheFranchiseHasNoProducts() {
        assertThat(adapter.findTopProductsPerBranch(franchiseId)).isEmpty();
    }

    @Test
    void topProductsPerBranchIgnoresOtherFranchises() {
        UUID alien = franchiseRepository.saveAndFlush(new FranchiseJpaEntity("Alien")).getId();
        UUID alienBranch = adapter.save(Branch.create("Alfa", alien)).getId();
        adapter.addProduct(alienBranch, productId, 100);

        assertThat(adapter.findTopProductsPerBranch(franchiseId)).isEmpty();
    }
}