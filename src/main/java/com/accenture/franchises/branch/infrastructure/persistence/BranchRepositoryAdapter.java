package com.accenture.franchises.branch.infrastructure.persistence;

import com.accenture.franchises.branch.domain.Branch;
import com.accenture.franchises.branch.domain.BranchRepositoryPort;
import com.accenture.franchises.franchise.infrastructure.persistence.FranchiseJpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class BranchRepositoryAdapter implements BranchRepositoryPort {

    private final BranchJpaRepository repository;
    private final FranchiseJpaRepository franchises;

    public BranchRepositoryAdapter(BranchJpaRepository repository, FranchiseJpaRepository franchises) {
        this.repository = repository;
        this.franchises = franchises;
    }

    @Override
    public Branch save(Branch branch) {
        BranchJpaEntity entity = new BranchJpaEntity(
                branch.getName(),
                franchises.getReferenceById(branch.getFranchiseId()));

        return BranchJpaMapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public boolean existsByNameInFranchise(String name, UUID franchiseId) {
        return repository.existsByNameIgnoreCaseAndFranchiseId(name, franchiseId);
    }
}