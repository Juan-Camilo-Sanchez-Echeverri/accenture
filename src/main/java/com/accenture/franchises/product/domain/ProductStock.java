package com.accenture.franchises.product.domain;

import java.util.UUID;

public record ProductStock(UUID branchId, String branchName, int stock) {
}