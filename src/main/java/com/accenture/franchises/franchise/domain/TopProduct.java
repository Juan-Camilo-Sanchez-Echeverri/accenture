package com.accenture.franchises.franchise.domain;

import java.util.UUID;

public record TopProduct(UUID branchId, String branchName, UUID productId, String productName, int stock) {
}