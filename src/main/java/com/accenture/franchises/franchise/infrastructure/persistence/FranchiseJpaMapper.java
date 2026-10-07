package com.accenture.franchises.franchise.infrastructure.persistence;

import com.accenture.franchises.franchise.domain.Franchise;

public final class FranchiseJpaMapper {

    private FranchiseJpaMapper() {
    }

    public static Franchise toDomain(FranchiseJpaEntity entity) {
        return Franchise.restore(entity.getId(), entity.getName(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
