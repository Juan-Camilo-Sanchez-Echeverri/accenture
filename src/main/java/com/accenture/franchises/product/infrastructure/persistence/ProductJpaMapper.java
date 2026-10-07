package com.accenture.franchises.product.infrastructure.persistence;

import com.accenture.franchises.product.domain.Product;
import com.accenture.franchises.product.domain.ProductStock;
import java.util.List;

public final class ProductJpaMapper {

    private ProductJpaMapper() {
    }

    public static Product toDomain(ProductJpaEntity entity) {
        return Product.restore(entity.getId(), entity.getName(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public static Product toDomain(ProductJpaEntity entity, List<ProductStock> stocks) {
        return Product.restore(entity.getId(), entity.getName(), entity.getCreatedAt(), entity.getUpdatedAt(), stocks);
    }
}