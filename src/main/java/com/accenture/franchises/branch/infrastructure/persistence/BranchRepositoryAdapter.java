package com.accenture.franchises.branch.infrastructure.persistence;

import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.branch.domain.BranchProduct;
import com.accenture.franchises.branch.domain.BranchProductSummary;
import com.accenture.franchises.branch.domain.BranchRepositoryPort;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import com.accenture.franchises.franchise.infrastructure.persistence.FranchiseJpaRepository;
import com.accenture.franchises.product.infrastructure.persistence.ProductJpaRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class BranchRepositoryAdapter implements BranchRepositoryPort {

    private static final String TYPE = "Branch";

    private final BranchJpaRepository repository;
    private final FranchiseJpaRepository franchises;
    private final ProductJpaRepository products;
    private final BranchProductJpaRepository stock;
    private final EntityManager entityManager;

    public BranchRepositoryAdapter(
            BranchJpaRepository repository,
            FranchiseJpaRepository franchises,
            ProductJpaRepository products,
            BranchProductJpaRepository stock,
            EntityManager entityManager) {
        this.repository = repository;
        this.franchises = franchises;
        this.products = products;
        this.stock = stock;
        this.entityManager = entityManager;
    }

    @Override
    public Branch save(Branch branch) {
        BranchJpaEntity entity = branch.getId() == null
                ? new BranchJpaEntity(
                        branch.getName(),
                        franchises.getReferenceById(branch.getFranchiseId()))
                : renameExisting(branch.getId(), branch.getName());

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
    public List<Branch> findAllByFranchise(UUID franchiseId) {
        return repository.findAllByFranchiseIdOrderByCreatedAtAsc(franchiseId).stream()
                .map(BranchJpaMapper::toDomain)
                .toList();
    }

    @Override
    public PageResult<Branch> findAll(PageQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.limit(), Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<BranchJpaEntity> page = repository.findAll(pageable);

        return PageResult.of(
                page.getContent().stream().map(BranchJpaMapper::toDomain).toList(),
                query.page(),
                query.limit(),
                page.getTotalElements());
    }

    @Override
    public List<BranchProductSummary> findProducts(UUID branchId) {
        return stock.findAllProductsByBranchId(branchId).stream()
                .map(row -> new BranchProductSummary(
                        row.getId().getProductId(),
                        row.getProduct().getName(),
                        row.getStock()))
                .toList();
    }

    @Override
    public void delete(UUID branchId) {
        repository.deleteById(branchId);
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

    @Override
    public void removeProduct(UUID branchId, UUID productId) {
        stock.findById(new BranchProductId(branchId, productId))
                .ifPresent(entityManager::remove);
    }

    @Override
    public BranchProduct updateStock(UUID branchId, UUID productId, int quantity) {
        BranchProductJpaEntity entity = stock.findById(new BranchProductId(branchId, productId))
                .orElseThrow();

        entity.setStock(quantity);
        entityManager.flush();
        return BranchProductJpaMapper.toDomain(entity);
    }

    private BranchJpaEntity renameExisting(UUID branchId, String name) {
        BranchJpaEntity entity = repository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(TYPE, branchId));

        entity.setName(name);
        return entity;
    }
}