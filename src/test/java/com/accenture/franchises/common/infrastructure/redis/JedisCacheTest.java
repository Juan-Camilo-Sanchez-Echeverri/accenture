package com.accenture.franchises.common.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.RedisClient;

class JedisCacheTest {

    private static final RedisClient client = openClient();

    private final JedisCache cache = new JedisCache(client);
    private final String key = "test-cache:" + UUID.randomUUID();

    @Test
    void putStoresTheValueAndGetReadsItBack() {
        cache.put(key, "hola", 60);

        assertThat(cache.get(key)).contains("hola");
    }

    @Test
    void getReturnsEmptyForAMissingKey() {
        assertThat(cache.get(UUID.randomUUID().toString())).isEmpty();
    }

    @Test
    void deleteRemovesTheKey() {
        cache.put(key, "valor", 60);

        cache.delete(key);

        assertThat(cache.get(key)).isEmpty();
    }

    @Test
    void valueExpiresAfterTheTtl() throws InterruptedException {
        cache.put(key, "temporal", 1);

        Thread.sleep(1200);

        assertThat(cache.get(key)).isEmpty();
    }

    @AfterEach
    void cleanUp() {
        client.del(key);
    }

    @AfterAll
    static void close() {
        client.close();
    }

    private static RedisClient openClient() {
        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String password = System.getenv().getOrDefault("REDIS_PASSWORD", "");
        if (password.isBlank()) {
            return RedisClient.create(host, port);
        }
        return RedisClient.create(host, port, "default", password);
    }
}