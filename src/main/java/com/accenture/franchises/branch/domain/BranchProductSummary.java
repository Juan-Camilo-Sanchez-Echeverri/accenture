package com.accenture.franchises.branch.domain;

import java.util.UUID;

public record BranchProductSummary(UUID productId, String productName, int stock) {
}