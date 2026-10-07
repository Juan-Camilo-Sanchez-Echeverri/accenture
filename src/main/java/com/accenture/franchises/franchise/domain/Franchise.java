package com.accenture.franchises.franchise.domain;

import java.time.Instant;
import java.util.UUID;

public class Franchise {

    private final UUID id;
    private final String name;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Franchise(UUID id, String name, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Franchise create(String name) {
        return new Franchise(null, name, null, null);
    }

    public static Franchise restore(UUID id, String name, Instant createdAt, Instant updatedAt) {
        return new Franchise(id, name, createdAt, updatedAt);
    }

    public Franchise rename(String name) {
        return new Franchise(id, name, createdAt, updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
