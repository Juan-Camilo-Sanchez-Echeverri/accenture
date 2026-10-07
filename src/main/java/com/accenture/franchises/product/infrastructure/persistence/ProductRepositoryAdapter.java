package com.accenture.franchises.product.infrastructure.persistence;

import com.accenture.franchises.branch.infrastructure.persistence.BranchProductJpaEntity;
import com.accenture.franchises.branch.infrastructure.persistence.BranchProductJpaRepository;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import com.accenture.franchises.product.domain.Product;
import com.accenture.franchises.product.domain.ProductRepositoryPort;
import com.accenture.franchises.product.domain.ProductStock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private static final String TYPE = "Product";

    private final ProductJpaRepository repository;
    private final BranchProductJpaRepository stock;

    public ProductRepositoryAdapter(ProductJpaRepository repository, BranchProductJpaRepository stock) {
        this.repository = repository;
        this.stock = stock;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = product.getId() == null
                ? new ProductJpaEntity(product.getName())
                : renameExisting(product.getId(), product.getName());

        ProductJpaEntity saved = repository.saveAndFlush(entity);
        return ProductJpaMapper.toDomain(saved, findStocks(saved.getId()));
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return repository.findById(id)
                .map(entity -> ProductJpaMapper.toDomain(entity, findStocks(entity.getId())));
    }

    @Override
    public PageResult<Product> findAll(PageQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.limit(), Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<ProductJpaEntity> page = repository.findAll(pageable);

        List<UUID> ids = page.getContent().stream().map(ProductJpaEntity::getId).toList();
        Map<UUID, List<ProductStock>> stocksByProduct = findStocksByProduct(ids);

        return PageResult.of(
                page.getContent().stream()
                        .map(entity -> ProductJpaMapper.toDomain(
                                entity,
                                stocksByProduct.getOrDefault(entity.getId(), List.of())))
                        .toList(),
                query.page(),
                query.limit(),
                page.getTotalElements());
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private List<ProductStock> findStocks(UUID productId) {
        return findStocksByProduct(List.of(productId)).getOrDefault(productId, List.of());
    }

    private Map<UUID, List<ProductStock>> findStocksByProduct(Collection<UUID> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, List<ProductStock>> grouped = new HashMap<>();

        for (BranchProductJpaEntity row : stock.findAllByProductIdIn(productIds)) {
            grouped.computeIfAbsent(row.getId().getProductId(), unused -> new ArrayList<>())
                    .add(new ProductStock(row.getId().getBranchId(), row.getBranch().getName(), row.getStock()));
        }

        grouped.values()
                .forEach(list -> list.sort(Comparator.comparing(ProductStock::branchName)));
        return grouped;
    }

    private ProductJpaEntity renameExisting(UUID id, String name) {
        ProductJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TYPE, id));

        entity.setName(name);
        return entity;
    }
}