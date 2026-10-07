package com.accenture.franchises.branch.infrastructure.persistence;

import com.accenture.franchises.product.infrastructure.persistence.ProductJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "branch_product")
public class BranchProductJpaEntity implements Persistable<BranchProductId> {

    @EmbeddedId
    private BranchProductId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BranchJpaEntity branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProductJpaEntity product;

    @Column(nullable = false)
    private int stock;

    protected BranchProductJpaEntity() {
    }

    public BranchProductJpaEntity(BranchProductId id, BranchJpaEntity branch, ProductJpaEntity product, int stock) {
        this.id = id;
        this.branch = branch;
        this.product = product;
        this.stock = stock;
    }

    public BranchProductId getId() {
        return id;
    }

    public BranchJpaEntity getBranch() {
        return branch;
    }

    public ProductJpaEntity getProduct() {
        return product;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}