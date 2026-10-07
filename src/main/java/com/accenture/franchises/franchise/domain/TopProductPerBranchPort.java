package com.accenture.franchises.franchise.domain;

import java.util.List;
import java.util.UUID;

public interface TopProductPerBranchPort {

    List<TopProduct> findTopProductPerBranch(UUID franchiseId);
}