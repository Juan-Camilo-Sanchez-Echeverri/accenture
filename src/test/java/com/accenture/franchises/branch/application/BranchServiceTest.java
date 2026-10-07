package com.accenture.franchises.branch.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.branch.domain.BranchDetail;
import com.accenture.franchises.branch.domain.BranchProduct;
import com.accenture.franchises.branch.domain.BranchProductSummary;
import com.accenture.franchises.branch.domain.BranchRepositoryPort;
import com.accenture.franchises.franchise.domain.Franchise;
import com.accenture.franchises.franchise.domain.FranchiseRepositoryPort;
import com.accenture.franchises.common.cache.CacheKeys;
import com.accenture.franchises.common.cache.CachePort;
import com.accenture.franchises.common.exception.ConflictException;
import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import com.accenture.franchises.product.domain.Product;
import com.accenture.franchises.product.domain.ProductRepositoryPort;
import java.time.Instant;
import java.util.List;
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

    @Mock
    private CachePort cache;

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
        verify(cache).delete(CacheKeys.topProducts(franchiseId));
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
    void renameUpdatesTheName() {
        existingBranch();
        when(branches.existsByNameInFranchise("Norte", franchiseId)).thenReturn(false);
        when(branches.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Branch renamed = service.rename(branchId, "Norte");

        assertThat(renamed.getName()).isEqualTo("Norte");
        verify(branches).save(any());
        verify(cache).delete(CacheKeys.topProducts(franchiseId));
    }

    @Test
    void renameKeepsTheSameNameWhenOnlyCaseChanges() {
        existingBranch();
        when(branches.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Branch renamed = service.rename(branchId, "centro");

        assertThat(renamed.getName()).isEqualTo("centro");
        verify(branches, never()).existsByNameInFranchise(any(), any());
        verify(branches).save(any());
        verify(cache).delete(CacheKeys.topProducts(franchiseId));
    }

    @Test
    void renameRejectsNameTakenByAnotherBranch() {
        existingBranch();
        when(branches.existsByNameInFranchise("Almacén", franchiseId)).thenReturn(true);

        assertThatThrownBy(() -> service.rename(branchId, "Almacén"))
                .isInstanceOf(DuplicateNameException.class);

        verify(branches, never()).save(any());
    }

    @Test
    void deleteRemovesTheBranch() {
        existingBranch();

        service.delete(branchId);

        verify(branches).delete(branchId);
        verify(cache).delete(CacheKeys.topProducts(franchiseId));
    }

    @Test
    void deleteFailsWhenBranchIsMissing() {
        when(branches.findById(branchId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(branchId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).delete(any());
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
        verify(cache).delete(CacheKeys.topProducts(franchiseId));
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

    @Test
    void removeProductDeletesTheLink() {
        existingBranch();
        existingProduct();
        when(branches.findProductStock(branchId, productId))
                .thenReturn(Optional.of(BranchProduct.restore(branchId, productId, 5)));

        service.removeProduct(branchId, productId);

        verify(branches).removeProduct(branchId, productId);
        verify(cache).delete(CacheKeys.topProducts(franchiseId));
    }

    @Test
    void removeProductFailsWhenBranchIsMissing() {
        when(branches.findById(branchId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeProduct(branchId, productId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).removeProduct(any(), any());
    }

    @Test
    void removeProductFailsWhenProductIsMissing() {
        existingBranch();
        when(products.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeProduct(branchId, productId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).removeProduct(any(), any());
    }

    @Test
    void removeProductFailsWhenProductIsNotLinked() {
        existingBranch();
        existingProduct();
        when(branches.findProductStock(branchId, productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeProduct(branchId, productId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).removeProduct(any(), any());
    }

    @Test
    void updateStockChangesTheQuantity() {
        existingBranch();
        existingProduct();
        when(branches.findProductStock(branchId, productId))
                .thenReturn(Optional.of(BranchProduct.restore(branchId, productId, 5)));
        when(branches.updateStock(branchId, productId, 25))
                .thenReturn(BranchProduct.restore(branchId, productId, 25));

        BranchProduct updated = service.updateStock(branchId, productId, 25);

        assertThat(updated.getStock()).isEqualTo(25);
        verify(cache).delete(CacheKeys.topProducts(franchiseId));
    }

    @Test
    void updateStockFailsWhenBranchIsMissing() {
        when(branches.findById(branchId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStock(branchId, productId, 25))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).updateStock(any(), any(), anyInt());
    }

    @Test
    void updateStockFailsWhenProductIsMissing() {
        existingBranch();
        when(products.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStock(branchId, productId, 25))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).updateStock(any(), any(), anyInt());
    }

    @Test
    void updateStockFailsWhenProductIsNotLinked() {
        existingBranch();
        existingProduct();
        when(branches.findProductStock(branchId, productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStock(branchId, productId, 25))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).updateStock(any(), any(), anyInt());
    }

    @Test
    void findAllByFranchiseReturnsTheBranchesOfTheFranchise() {
        existingFranchise();
        Branch centro = Branch.restore(branchId, "Centro", franchiseId, now, now);
        when(branches.findAllByFranchise(franchiseId)).thenReturn(List.of(centro));

        List<Branch> result = service.findAllByFranchise(franchiseId);

        assertThat(result).containsExactly(centro);
    }

    @Test
    void findAllByFranchiseFailsWhenFranchiseIsMissing() {
        when(franchises.findById(franchiseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findAllByFranchise(franchiseId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).findAllByFranchise(any());
    }

    @Test
    void findAllDelegatesTheQueryToThePort() {
        PageQuery query = PageQuery.of(1, 10);
        PageResult<Branch> expected = PageResult.of(List.of(), 1, 10, 0);
        when(branches.findAll(query)).thenReturn(expected);

        assertThat(service.findAll(query)).isSameAs(expected);
    }

    @Test
    void findDetailReturnsTheBranchWithItsProducts() {
        existingBranch();
        BranchProductSummary bigMac = new BranchProductSummary(productId, "Big Mac", 15);
        when(branches.findProducts(branchId)).thenReturn(List.of(bigMac));

        BranchDetail detail = service.findDetail(branchId);

        assertThat(detail.branch().getName()).isEqualTo("Centro");
        assertThat(detail.products()).containsExactly(bigMac);
    }

    @Test
    void findDetailFailsWhenBranchIsMissing() {
        when(branches.findById(branchId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findDetail(branchId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branches, never()).findProducts(any());
    }
}