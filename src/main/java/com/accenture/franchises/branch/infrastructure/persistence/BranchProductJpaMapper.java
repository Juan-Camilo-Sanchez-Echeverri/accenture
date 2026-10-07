package com.accenture.franchises.branch.infrastructure.persistence;

import com.accenture.franchises.branch.domain.BranchProduct;
import com.accenture.franchises.product.infrastructure.persistence.ProductJpaEntity;

public final class BranchProductJpaMapper {

    private BranchProductJpaMapper() {
    }

    public static BranchProduct toDomain(BranchProductJpaEntity entity) {
        return BranchProduct.restore(
                entity.getId().getBranchId(),
                entity.getId().getProductId(),
                entity.getStock());
    }

    public static BranchProductJpaEntity toEntity(BranchProduct branchProduct, BranchJpaEntity branch,
            ProductJpaEntity product) {
        return new BranchProductJpaEntity(
                new BranchProductId(branchProduct.getBranchId(), branchProduct.getProductId()),
                branch,
                product,
                branchProduct.getStock());
    }
}