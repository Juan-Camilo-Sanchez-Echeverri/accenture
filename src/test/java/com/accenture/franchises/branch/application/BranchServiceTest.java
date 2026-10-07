package com.accenture.franchises.branch.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.branch.domain.BranchProduct;
import com.accenture.franchises.branch.domain.BranchRepositoryPort;
import com.accenture.franchises.franchise.domain.Franchise;
import com.accenture.franchises.franchise.domain.FranchiseRepositoryPort;
import com.accenture.franchises.common.exception.ConflictException;
import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.product.domain.Product;
import com.accenture.franchises.product.domain.ProductRepositoryPort;
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

    @Mock
    private ProductRepositoryPort products;

    @InjectMocks
    private BranchService service;

    private final UUID franchiseId = UUID.randomUUID();
    private final UUID branchId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-01-01T10:00:00Z");

    private void existingFranchise() {
        when(franchises.findById(franchiseId))
                .thenReturn(Optional.of(Franchise.restore(franchiseId, "Acme", now, now)));
    }

    private void existingBranch() {
        when(branches.findById(branchId))
                .thenReturn(Optional.of(Branch.restore(branchId, "Centro", franchiseId, now, now)));
    }

    private void existingProduct() {
        when(products.findById(productId))
                .thenReturn(Optional.of(Product.restore(productId, "Hamburguesa", now, now)));
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

    @Test
    void addProductSavesTheStock() {
        existingBranch();
        existingProduct();
        when(branches.findProductStock(branchId, productId)).thenReturn(Optional.empty());
        when(branches.addProduct(branchId, productId, 10))
                .thenReturn(BranchProduct.restore(branchId, productId, 10));

        BranchProduct linked = service.addProduct(branchId, productId, 10);

        assertThat(linked.getBranchId()).isEqualTo(branchId);
        assertThat(linked.getProductId()).isEqualTo(productId);
        assertThat(linked.getStock()).isEqualTo(10);
    }

    @Test
    void addProductFailsWhenBranchIsMissing() {
        when(branches.findById(branchId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addProduct(branchId, productId, 10))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).addProduct(any(), any(), anyInt());
    }

    @Test
    void addProductFailsWhenProductIsMissing() {
        existingBranch();
        when(products.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addProduct(branchId, productId, 10))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).addProduct(any(), any(), anyInt());
    }

    @Test
    void addProductRejectsAlreadyLinkedProduct() {
        existingBranch();
        existingProduct();
        when(branches.findProductStock(branchId, productId))
                .thenReturn(Optional.of(BranchProduct.restore(branchId, productId, 10)));

        assertThatThrownBy(() -> service.addProduct(branchId, productId, 10))
                .isInstanceOf(ConflictException.class);

        verify(branches, never()).addProduct(any(), any(), anyInt());
    }
}