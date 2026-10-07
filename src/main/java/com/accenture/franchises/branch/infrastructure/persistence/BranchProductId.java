package com.accenture.franchises.branch.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class BranchProductId implements Serializable {

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    protected BranchProductId() {
    }

    public BranchProductId(UUID branchId, UUID productId) {
        this.branchId = branchId;
        this.productId = productId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public UUID getProductId() {
        return productId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof BranchProductId that)) {
            return false;
        }

        return Objects.equals(branchId, that.branchId) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId, productId);
    }
}