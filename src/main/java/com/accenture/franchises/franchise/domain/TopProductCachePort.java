package com.accenture.franchises.franchise.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopProductCachePort {

    Optional<List<TopProduct>> get(UUID franchiseId);

    void put(UUID franchiseId, List<TopProduct> topProducts);
}