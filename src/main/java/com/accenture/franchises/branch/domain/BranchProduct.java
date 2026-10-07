package com.accenture.franchises.branch.domain;

import java.util.UUID;

public class BranchProduct {

    private final UUID branchId;
    private final UUID productId;
    private final int stock;

    private BranchProduct(UUID branchId, UUID productId, int stock) {
        this.branchId = branchId;
        this.productId = productId;
        this.stock = stock;
    }

    public static BranchProduct restore(UUID branchId, UUID productId, int stock) {
        return new BranchProduct(branchId, productId, stock);
    }

    public UUID getBranchId() {
        return branchId;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getStock() {
        return stock;
    }
}