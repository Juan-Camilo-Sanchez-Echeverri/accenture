package com.accenture.franchises.product.infrastructure.persistence;

import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import com.accenture.franchises.product.domain.Product;
import com.accenture.franchises.product.domain.ProductRepositoryPort;
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

    public ProductRepositoryAdapter(ProductJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = product.getId() == null
                ? new ProductJpaEntity(product.getName())
                : renameExisting(product.getId(), product.getName());

        return ProductJpaMapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return repository.findById(id).map(ProductJpaMapper::toDomain);
    }

    @Override
    public PageResult<Product> findAll(PageQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.limit(), Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<ProductJpaEntity> page = repository.findAll(pageable);

        return PageResult.of(
                page.getContent().stream().map(ProductJpaMapper::toDomain).toList(),
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

    private ProductJpaEntity renameExisting(UUID id, String name) {
        ProductJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TYPE, id));

        entity.setName(name);
        return entity;
    }
}