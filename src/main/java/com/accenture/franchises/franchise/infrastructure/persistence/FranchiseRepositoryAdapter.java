package com.accenture.franchises.franchise.infrastructure.persistence;

import com.accenture.franchises.franchise.domain.Franchise;
import com.accenture.franchises.franchise.domain.FranchiseRepositoryPort;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class FranchiseRepositoryAdapter implements FranchiseRepositoryPort {

    private static final String TYPE = "Franchise";

    private final FranchiseJpaRepository repository;

    public FranchiseRepositoryAdapter(FranchiseJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Franchise save(Franchise franchise) {
        FranchiseJpaEntity entity = franchise.getId() == null
                ? new FranchiseJpaEntity(franchise.getName())
                : renameExisting(franchise.getId(), franchise.getName());

        return FranchiseJpaMapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<Franchise> findById(UUID id) {
        return repository.findById(id).map(FranchiseJpaMapper::toDomain);
    }

    @Override
    public PageResult<Franchise> findAll(PageQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.limit(), Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<FranchiseJpaEntity> page = repository.findAll(pageable);

        return PageResult.of(
                page.getContent().stream().map(FranchiseJpaMapper::toDomain).toList(),
                query.page(),
                query.limit(),
                page.getTotalElements());
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private FranchiseJpaEntity renameExisting(UUID id, String name) {
        FranchiseJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TYPE, id));

        entity.setName(name);
        return entity;
    }
}
