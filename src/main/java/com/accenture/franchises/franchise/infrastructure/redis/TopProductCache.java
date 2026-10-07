package com.accenture.franchises.franchise.infrastructure.redis;

import com.accenture.franchises.common.cache.CacheKeys;
import com.accenture.franchises.common.cache.CachePort;
import com.accenture.franchises.franchise.domain.TopProduct;
import com.accenture.franchises.franchise.domain.TopProductCachePort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TopProductCache implements TopProductCachePort {

    private static final Logger log = LoggerFactory.getLogger(TopProductCache.class);
    private static final TypeReference<List<TopProduct>> TOP_PRODUCTS = new TypeReference<>() {
    };

    private final CachePort cache;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final long ttlSeconds;

    public TopProductCache(
            CachePort cache,
            @Value("${top-products.cache.ttl-seconds:60}") long ttlSeconds) {

        this.cache = cache;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public Optional<List<TopProduct>> get(UUID franchiseId) {
        Optional<String> raw = cache.get(CacheKeys.topProducts(franchiseId));
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(raw.get(), TOP_PRODUCTS));
        } catch (IOException e) {
            log.warn("Could not deserialize the top products of franchise {}", franchiseId, e);
            return Optional.empty();
        }
    }

    @Override
    public void put(UUID franchiseId, List<TopProduct> topProducts) {
        try {
            cache.put(CacheKeys.topProducts(franchiseId),
                    objectMapper.writeValueAsString(topProducts),
                    ttlSeconds);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize the top products of franchise {}", franchiseId, e);
        }
    }
}