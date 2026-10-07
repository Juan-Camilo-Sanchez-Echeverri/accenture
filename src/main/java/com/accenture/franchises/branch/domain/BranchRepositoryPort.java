package com.accenture.franchises.branch.domain;

import java.util.UUID;

public interface BranchRepositoryPort {

    Branch save(Branch branch);

    boolean existsByNameInFranchise(String name, UUID franchiseId);
}