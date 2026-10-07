package com.accenture.franchises.branch.infrastructure.persistence;

import com.accenture.franchises.franchise.infrastructure.persistence.FranchiseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "branch", uniqueConstraints = @UniqueConstraint(
        name = "uq_branch_name_in_franchise",
        columnNames = { "franchise_id", "name" }))
public class BranchJpaEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "franchise_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private FranchiseJpaEntity franchise;

    @CreationTimestamp(source = SourceType.VM)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.VM)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BranchJpaEntity() {
    }

    public BranchJpaEntity(String name, FranchiseJpaEntity franchise) {
        this.name = name;
        this.franchise = franchise;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FranchiseJpaEntity getFranchise() {
        return franchise;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}