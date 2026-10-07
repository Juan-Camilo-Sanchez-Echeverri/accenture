package com.accenture.franchises.branch.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BranchProductJpaRepository extends JpaRepository<BranchProductJpaEntity, BranchProductId> {

    @Query("""
            select bp from BranchProductJpaEntity bp
            join fetch bp.branch
            where bp.id.productId in :ids
            """)
    List<BranchProductJpaEntity> findAllByProductIdIn(@Param("ids") Collection<UUID> ids);
}