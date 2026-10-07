package com.accenture.franchises.product.domain;

import java.time.Instant;
import java.util.UUID;

public class Product {

    private final UUID id;
    private final String name;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Product(UUID id, String name, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Product create(String name) {
        return new Product(null, name, null, null);
    }

    public static Product restore(UUID id, String name, Instant createdAt, Instant updatedAt) {
        return new Product(id, name, createdAt, updatedAt);
    }

    public Product rename(String name) {
        return new Product(id, name, createdAt, updatedAt);
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