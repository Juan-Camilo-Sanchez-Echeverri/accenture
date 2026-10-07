package com.accenture.franchises.branch.infrastructure.persistence;

import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.branch.domain.BranchProduct;
import com.accenture.franchises.branch.domain.BranchRepositoryPort;
import com.accenture.franchises.franchise.infrastructure.persistence.FranchiseJpaRepository;
import com.accenture.franchises.product.infrastructure.persistence.ProductJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class BranchRepositoryAdapter implements BranchRepositoryPort {

    private final BranchJpaRepository repository;
    private final FranchiseJpaRepository franchises;
    private final ProductJpaRepository products;
    private final BranchProductJpaRepository stock;

    public BranchRepositoryAdapter(
            BranchJpaRepository repository,
            FranchiseJpaRepository franchises,
            ProductJpaRepository products,
            BranchProductJpaRepository stock) {
        this.repository = repository;
        this.franchises = franchises;
        this.products = products;
        this.stock = stock;
    }

    @Override
    public Branch save(Branch branch) {
        BranchJpaEntity entity = new BranchJpaEntity(
                branch.getName(),
                franchises.getReferenceById(branch.getFranchiseId()));

        return BranchJpaMapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public boolean existsByNameInFranchise(String name, UUID franchiseId) {
        return repository.existsByNameIgnoreCaseAndFranchiseId(name, franchiseId);
    }

    @Override
    public Optional<Branch> findById(UUID id) {
        return repository.findById(id).map(BranchJpaMapper::toDomain);
    }

    @Override
    public Optional<BranchProduct> findProductStock(UUID branchId, UUID productId) {
        return stock.findById(new BranchProductId(branchId, productId))
                .map(BranchProductJpaMapper::toDomain);
    }

    @Override
    public BranchProduct addProduct(UUID branchId, UUID productId, int quantity) {
        BranchProductJpaEntity saved = this.stock.saveAndFlush(
                BranchProductJpaMapper.toEntity(
                        BranchProduct.restore(branchId, productId, quantity),
                        repository.getReferenceById(branchId),
                        products.getReferenceById(productId)));

        return BranchProductJpaMapper.toDomain(saved);
    }
}