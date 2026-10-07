package com.accenture.franchises.branch.domain;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepositoryPort {

    Branch save(Branch branch);

    boolean existsByNameInFranchise(String name, UUID franchiseId);

    Optional<Branch> findById(UUID id);

    Optional<BranchProduct> findProductStock(UUID branchId, UUID productId);

    BranchProduct addProduct(UUID branchId, UUID productId, int stock);

    void removeProduct(UUID branchId, UUID productId);
}