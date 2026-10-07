package com.accenture.franchises.product.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Product {

    private final UUID id;
    private final String name;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final List<ProductStock> stocks;

    private Product(UUID id, String name, Instant createdAt, Instant updatedAt, List<ProductStock> stocks) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stocks = List.copyOf(stocks);
    }

    public static Product create(String name) {
        return new Product(null, name, null, null, List.of());
    }

    public static Product restore(UUID id, String name, Instant createdAt, Instant updatedAt) {
        return restore(id, name, createdAt, updatedAt, List.of());
    }

    public static Product restore(UUID id, String name, Instant createdAt, Instant updatedAt,
            List<ProductStock> stocks) {
        return new Product(id, name, createdAt, updatedAt, stocks);
    }

    public Product rename(String name) {
        return new Product(id, name, createdAt, updatedAt, stocks);
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

    public List<ProductStock> getStocks() {
        return stocks;
    }
}