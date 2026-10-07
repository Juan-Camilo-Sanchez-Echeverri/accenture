package com.accenture.franchises.franchise.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.accenture.franchises.branch.infrastructure.persistence.BranchJpaEntity;
import com.accenture.franchises.branch.infrastructure.persistence.BranchJpaRepository;
import com.accenture.franchises.branch.infrastructure.persistence.BranchProductJpaEntity;
import com.accenture.franchises.branch.infrastructure.persistence.BranchProductId;
import com.accenture.franchises.branch.infrastructure.persistence.BranchProductJpaRepository;
import com.accenture.franchises.franchise.domain.TopProduct;
import com.accenture.franchises.product.infrastructure.persistence.ProductJpaEntity;
import com.accenture.franchises.product.infrastructure.persistence.ProductJpaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TopProductPerBranchAdapter.class)
class TopProductPerBranchAdapterTest {

    @Autowired
    private TopProductPerBranchAdapter adapter;

    @Autowired
    private FranchiseJpaRepository franchiseRepository;

    @Autowired
    private BranchJpaRepository branchRepository;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private BranchProductJpaRepository stockRepository;

    private UUID franchiseId;

    @BeforeEach
    void setUp() {
        franchiseId = franchiseRepository.saveAndFlush(new FranchiseJpaEntity("Acme")).getId();
    }

    @Test
    void returnsTheBestProductOfEachBranch() {
        UUID product1 = productRepository.saveAndFlush(new ProductJpaEntity("Pizza")).getId();
        UUID product2 = productRepository.saveAndFlush(new ProductJpaEntity("Coca")).getId();
        UUID branchA = createBranch("North");
        UUID branchB = createBranch("South");
        link(branchA, product2, 5);
        link(branchA, product1, 20);
        link(branchB, product2, 30);

        List<TopProduct> top = adapter.findTopProductPerBranch(franchiseId);

        assertThat(top).extracting(TopProduct::branchName).containsExactly("North", "South");
        assertThat(top).extracting(TopProduct::productId).containsExactly(product1, product2);
        assertThat(top).extracting(TopProduct::stock).containsExactly(20, 30);
    }

    @Test
    void isEmptyWhenTheFranchiseHasNoProducts() {
        assertThat(adapter.findTopProductPerBranch(franchiseId)).isEmpty();
    }

    @Test
    void ignoresOtherFranchises() {
        UUID alien = franchiseRepository.saveAndFlush(new FranchiseJpaEntity("Alien")).getId();
        UUID alienBranch = createBranch("Alfa", alien);
        UUID product = productRepository.saveAndFlush(new ProductJpaEntity("Coca")).getId();
        link(alienBranch, product, 100);

        assertThat(adapter.findTopProductPerBranch(franchiseId)).isEmpty();
    }

    private UUID createBranch(String name) {
        return createBranch(name, franchiseId);
    }

    private UUID createBranch(String name, UUID franchiseId) {
        FranchiseJpaEntity franchise = franchiseRepository.getReferenceById(franchiseId);
        return branchRepository.saveAndFlush(new BranchJpaEntity(name, franchise)).getId();
    }

    private void link(UUID branchId, UUID productId, int stock) {
        BranchJpaEntity branch = branchRepository.getReferenceById(branchId);
        ProductJpaEntity product = productRepository.getReferenceById(productId);
        stockRepository.saveAndFlush(new BranchProductJpaEntity(
                new BranchProductId(branchId, productId), branch, product, stock));
    }
}