package com.accenture.franchises.product.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    boolean existsByNameIgnoreCase(String name);
}