package com.accenture.franchises.branch.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchProductJpaRepository extends JpaRepository<BranchProductJpaEntity, BranchProductId> {
}