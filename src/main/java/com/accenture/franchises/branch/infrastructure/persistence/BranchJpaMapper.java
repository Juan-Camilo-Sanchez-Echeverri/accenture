package com.accenture.franchises.branch.infrastructure.persistence;

import com.accenture.franchises.branch.domain.Branch;

public final class BranchJpaMapper {

    private BranchJpaMapper() {
    }

    public static Branch toDomain(BranchJpaEntity entity) {
        return Branch.restore(
                entity.getId(),
                entity.getName(),
                entity.getFranchise().getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}