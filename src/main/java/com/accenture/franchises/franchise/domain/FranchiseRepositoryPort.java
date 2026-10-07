package com.accenture.franchises.franchise.domain;

import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import java.util.Optional;
import java.util.UUID;

public interface FranchiseRepositoryPort {

    Franchise save(Franchise franchise);

    Optional<Franchise> findById(UUID id);

    PageResult<Franchise> findAll(PageQuery query);

    boolean existsByNameIgnoreCase(String name);

    void delete(UUID id);
}
