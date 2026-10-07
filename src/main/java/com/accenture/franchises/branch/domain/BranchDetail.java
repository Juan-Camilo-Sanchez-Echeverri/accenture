package com.accenture.franchises.branch.domain;

import java.util.List;

public record BranchDetail(Branch branch, List<BranchProductSummary> products) {
}