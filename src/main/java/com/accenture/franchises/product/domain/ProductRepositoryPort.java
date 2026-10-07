package com.accenture.franchises.product.domain;

import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    PageResult<Product> findAll(PageQuery query);

    boolean existsByNameIgnoreCase(String name);

    void delete(UUID id);
}