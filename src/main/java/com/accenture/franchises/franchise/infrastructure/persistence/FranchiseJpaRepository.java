package com.accenture.franchises.franchise.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FranchiseJpaRepository extends JpaRepository<FranchiseJpaEntity, UUID> {

    boolean existsByNameIgnoreCase(String name);
}
