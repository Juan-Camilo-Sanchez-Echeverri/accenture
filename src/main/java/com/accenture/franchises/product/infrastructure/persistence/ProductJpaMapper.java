package com.accenture.franchises.product.infrastructure.persistence;

import com.accenture.franchises.product.domain.Product;

public final class ProductJpaMapper {

    private ProductJpaMapper() {
    }

    public static Product toDomain(ProductJpaEntity entity) {
        return Product.restore(entity.getId(), entity.getName(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}