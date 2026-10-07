package com.accenture.franchises.franchise.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;

import com.accenture.franchises.common.cache.CachePort;
import com.accenture.franchises.franchise.domain.TopProduct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TopProductCacheTest {

    private final StubCachePort stub = new StubCachePort();
    private final TopProductCache cache = new TopProductCache(stub, 60);

    @Test
    void putStoresTheTopProductsAndGetReadsThemBack() {
        UUID franchiseId = UUID.randomUUID();
        List<TopProduct> top = List.of(
                new TopProduct(UUID.randomUUID(), "Centro", UUID.randomUUID(), "Big Mac", 15));

        cache.put(franchiseId, top);

        assertThat(cache.get(franchiseId)).contains(top);
    }

    @Test
    void getReturnsEmptyWhenNothingWasCached() {
        assertThat(cache.get(UUID.randomUUID())).isEmpty();
    }

    @Test
    void getReturnsEmptyWhenThePayloadCannotBeRead() {
        UUID franchiseId = UUID.randomUUID();
        cache.put(franchiseId, List.of());

        stub.entries.put("franchise:top-products:" + franchiseId, "not-json");

        assertThat(cache.get(franchiseId)).isEmpty();
    }

    private static final class StubCachePort implements CachePort {

        private final Map<String, String> entries = new HashMap<>();

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(entries.get(key));
        }

        @Override
        public void put(String key, String value, long ttlSeconds) {
            entries.put(key, value);
        }

        @Override
        public void delete(String key) {
            entries.remove(key);
        }
    }
}