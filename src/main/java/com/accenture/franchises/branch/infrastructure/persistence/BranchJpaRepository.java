package com.accenture.franchises.branch.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchJpaRepository extends JpaRepository<BranchJpaEntity, UUID> {

    boolean existsByNameIgnoreCaseAndFranchiseId(String name, UUID franchiseId);

    List<BranchJpaEntity> findAllByFranchiseIdOrderByCreatedAtAsc(UUID franchiseId);
}