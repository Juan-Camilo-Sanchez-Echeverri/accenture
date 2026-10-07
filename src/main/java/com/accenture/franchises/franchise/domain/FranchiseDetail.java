package com.accenture.franchises.franchise.domain;

import com.accenture.franchises.branch.domain.Branch;
import java.util.List;

public record FranchiseDetail(Franchise franchise, List<Branch> branches) {
}