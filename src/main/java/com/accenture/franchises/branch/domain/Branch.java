package com.accenture.franchises.branch.domain;

import java.time.Instant;
import java.util.UUID;

public class Branch {

    private final UUID id;
    private final String name;
    private final UUID franchiseId;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Branch(UUID id, String name, UUID franchiseId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.franchiseId = franchiseId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Branch create(String name, UUID franchiseId) {
        return new Branch(null, name, franchiseId, null, null);
    }

    public static Branch restore(UUID id, String name, UUID franchiseId, Instant createdAt, Instant updatedAt) {
        return new Branch(id, name, franchiseId, createdAt, updatedAt);
    }

    public Branch rename(String name) {
        return new Branch(id, name, franchiseId, createdAt, updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getFranchiseId() {
        return franchiseId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}