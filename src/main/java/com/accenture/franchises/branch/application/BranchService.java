package com.accenture.franchises.branch.application;

import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.branch.domain.BranchRepositoryPort;
import com.accenture.franchises.franchise.domain.Franchise;
import com.accenture.franchises.franchise.domain.FranchiseRepositoryPort;
import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BranchService {

    private static final String TYPE = "Branch";

    private final BranchRepositoryPort branches;
    private final FranchiseRepositoryPort franchises;

    public BranchService(BranchRepositoryPort branches, FranchiseRepositoryPort franchises) {
        this.branches = branches;
        this.franchises = franchises;
    }

    @Transactional
    public Branch addBranch(UUID franchiseId, String name) {
        Franchise franchise = getExistingFranchise(franchiseId);
        if (branches.existsByNameInFranchise(name, franchise.getId())) {
            throw new DuplicateNameException(TYPE, name);
        }
        return branches.save(Branch.create(name, franchise.getId()));
    }

    private Franchise getExistingFranchise(UUID franchiseId) {
        return franchises.findById(franchiseId)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise", franchiseId));
    }
}