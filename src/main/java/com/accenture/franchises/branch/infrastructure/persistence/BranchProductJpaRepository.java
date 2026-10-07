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

    @Query("""
            select bp from BranchProductJpaEntity bp
            join fetch bp.branch b
            join fetch bp.product p
            where b.franchise.id = :franchiseId
            order by bp.stock desc, p.name asc
            """)
    List<BranchProductJpaEntity> findAllByBranchFranchiseId(@Param("franchiseId") UUID franchiseId);
}