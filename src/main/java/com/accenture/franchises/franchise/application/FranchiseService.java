package com.accenture.franchises.franchise.application;

import com.accenture.franchises.franchise.domain.Franchise;
import com.accenture.franchises.franchise.domain.FranchiseRepositoryPort;
import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FranchiseService {

    private static final String TYPE = "Franchise";

    private final FranchiseRepositoryPort franchises;

    public FranchiseService(FranchiseRepositoryPort franchises) {
        this.franchises = franchises;
    }

    @Transactional
    public Franchise create(String name) {
        if (franchises.existsByNameIgnoreCase(name)) {
            throw new DuplicateNameException(TYPE, name);
        }
        return franchises.save(Franchise.create(name));
    }

    public PageResult<Franchise> findAll(PageQuery query) {
        return franchises.findAll(query);
    }

    public Franchise findById(UUID id) {
        return getExisting(id);
    }

    @Transactional
    public Franchise rename(UUID id, String name) {
        Franchise franchise = getExisting(id);
        if (!franchise.getName().equalsIgnoreCase(name) && franchises.existsByNameIgnoreCase(name)) {
            throw new DuplicateNameException(TYPE, name);
        }
        return franchises.save(franchise.rename(name));
    }

    @Transactional
    public void delete(UUID id) {
        getExisting(id);
        franchises.delete(id);
    }

    private Franchise getExisting(UUID id) {
        return franchises.findById(id).orElseThrow(() -> new ResourceNotFoundException(TYPE, id));
    }
}
