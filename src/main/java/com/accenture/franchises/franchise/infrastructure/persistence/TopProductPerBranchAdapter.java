package com.accenture.franchises.franchise.infrastructure.persistence;

import com.accenture.franchises.franchise.domain.TopProduct;
import com.accenture.franchises.franchise.domain.TopProductPerBranchPort;
import jakarta.persistence.EntityManager;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class TopProductPerBranchAdapter implements TopProductPerBranchPort {

    private final EntityManager entityManager;

    public TopProductPerBranchAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<TopProduct> findTopProductPerBranch(UUID franchiseId) {
        List<Object[]> rows = entityManager.createQuery("""
                select b.id, b.name, bp.id.productId, p.name, bp.stock
                from BranchProductJpaEntity bp
                join bp.branch b
                join bp.product p
                where b.franchise.id = :franchiseId
                """, Object[].class)
                .setParameter("franchiseId", franchiseId)
                .getResultList();

        Map<UUID, TopProduct> bestByBranch = new HashMap<>();

        for (Object[] row : rows) {

            TopProduct candidate = new TopProduct(
                    (UUID) row[0],
                    (String) row[1],
                    (UUID) row[2],
                    (String) row[3],
                    ((Number) row[4]).intValue());

            TopProduct current = bestByBranch.get(candidate.branchId());

            if (current == null || candidate.stock() > current.stock()) {
                bestByBranch.put(candidate.branchId(), candidate);
            }
        }

        return bestByBranch.values().stream()
                .sorted(Comparator.comparing(TopProduct::branchName))
                .toList();
    }
}