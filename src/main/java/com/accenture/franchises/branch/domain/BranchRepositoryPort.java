package com.accenture.franchises.branch.domain;

import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepositoryPort {

    Branch save(Branch branch);

    boolean existsByNameInFranchise(String name, UUID franchiseId);

    Optional<Branch> findById(UUID id);

    List<Branch> findAllByFranchise(UUID franchiseId);

    PageResult<Branch> findAll(PageQuery query);

    List<BranchProductSummary> findProducts(UUID branchId);

    void delete(UUID branchId);

    Optional<BranchProduct> findProductStock(UUID branchId, UUID productId);

    BranchProduct addProduct(UUID branchId, UUID productId, int stock);

    void removeProduct(UUID branchId, UUID productId);

    BranchProduct updateStock(UUID branchId, UUID productId, int stock);
}