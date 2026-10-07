package com.accenture.franchises.product.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accenture.franchises.branch.infrastructure.persistence.BranchJpaEntity;
import com.accenture.franchises.branch.infrastructure.persistence.BranchJpaRepository;
import com.accenture.franchises.branch.infrastructure.persistence.BranchProductId;
import com.accenture.franchises.branch.infrastructure.persistence.BranchProductJpaEntity;
import com.accenture.franchises.branch.infrastructure.persistence.BranchProductJpaRepository;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import com.accenture.franchises.franchise.infrastructure.persistence.FranchiseJpaEntity;
import com.accenture.franchises.franchise.infrastructure.persistence.FranchiseJpaRepository;
import com.accenture.franchises.product.domain.Product;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProductRepositoryAdapter.class)
class ProductRepositoryAdapterTest {

    @Autowired
    private ProductRepositoryAdapter adapter;

    @Autowired
    private ProductJpaRepository repository;

    @Autowired
    private FranchiseJpaRepository franchiseRepository;

    @Autowired
    private BranchJpaRepository branchRepository;

    @Autowired
    private BranchProductJpaRepository stockRepository;

    @Test
    void saveAssignsIdAndTimestamps() {
        Product saved = adapter.save(Product.create("Hamburguesa"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void databaseRejectsDuplicatedName() {
        adapter.save(Product.create("Hamburguesa"));

        assertThatThrownBy(() -> adapter.save(Product.create("Hamburguesa")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void detectsExistingNameIgnoringCase() {
        adapter.save(Product.create("Hamburguesa"));

        assertThat(adapter.existsByNameIgnoreCase("hamburguesa")).isTrue();
        assertThat(adapter.existsByNameIgnoreCase("Pizza")).isFalse();
    }

    @Test
    void findAllReturnsAStablePage() {
        for (int i = 1; i <= 5; i++) {
            adapter.save(Product.create("Producto %02d".formatted(i)));
        }

        PageResult<Product> page = adapter.findAll(PageQuery.of(0, 2));

        assertThat(page.items()).hasSize(2);
        assertThat(page.page()).isZero();
        assertThat(page.limit()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.totalPages()).isEqualTo(3);
    }

    @Test
    void renameKeepsTheRowAndRefreshesTheTimestamp() {
        Product saved = adapter.save(Product.create("Hamburguesa"));
        adapter.save(saved.rename("Hamburguesa XL"));

        Product reloaded = adapter.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Hamburguesa XL");
        assertThat(reloaded.getCreatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void deleteRemovesTheRow() {
        Product saved = adapter.save(Product.create("Hamburguesa"));

        adapter.delete(saved.getId());

        assertThat(adapter.findById(saved.getId())).isEmpty();
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAllIncludesTheStockPerBranch() {
        FranchiseJpaEntity franchise = franchiseRepository.saveAndFlush(new FranchiseJpaEntity("Acme"));
        BranchJpaEntity branch = branchRepository.saveAndFlush(new BranchJpaEntity("Centro", franchise));
        Product saved = adapter.save(Product.create("Hamburguesa"));
        stockRepository.saveAndFlush(new BranchProductJpaEntity(
                new BranchProductId(branch.getId(), saved.getId()),
                branch,
                repository.getReferenceById(saved.getId()),
                12));

        PageResult<Product> page = adapter.findAll(PageQuery.of(0, 20));

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).getStocks()).hasSize(1);
        assertThat(page.items().get(0).getStocks().get(0).branchId()).isEqualTo(branch.getId());
        assertThat(page.items().get(0).getStocks().get(0).branchName()).isEqualTo("Centro");
        assertThat(page.items().get(0).getStocks().get(0).stock()).isEqualTo(12);
    }

    @Test
    void findByIdIncludesTheStockPerBranch() {
        FranchiseJpaEntity franchise = franchiseRepository.saveAndFlush(new FranchiseJpaEntity("Acme"));
        BranchJpaEntity branch = branchRepository.saveAndFlush(new BranchJpaEntity("Centro", franchise));
        Product saved = adapter.save(Product.create("Hamburguesa"));
        stockRepository.saveAndFlush(new BranchProductJpaEntity(
                new BranchProductId(branch.getId(), saved.getId()),
                branch,
                repository.getReferenceById(saved.getId()),
                5));

        Product reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getStocks()).hasSize(1);
        assertThat(reloaded.getStocks().get(0).branchId()).isEqualTo(branch.getId());
        assertThat(reloaded.getStocks().get(0).branchName()).isEqualTo("Centro");
        assertThat(reloaded.getStocks().get(0).stock()).isEqualTo(5);
    }
}