package com.accenture.franchises.common.infrastructure.redis;

import com.accenture.franchises.common.cache.CachePort;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.SetParams;

@Component
public class JedisCache implements CachePort {

    private static final Logger log = LoggerFactory.getLogger(JedisCache.class);

    private final RedisClient client;

    public JedisCache(RedisClient client) {
        this.client = client;
    }

    @Override
    public Optional<String> get(String key) {
        try {
            return Optional.ofNullable(client.get(key));
        } catch (RuntimeException e) {
            log.warn("Could not read cache key {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, String value, long ttlSeconds) {
        try {
            client.set(key, value, SetParams.setParams().ex(ttlSeconds));
        } catch (RuntimeException e) {
            log.warn("Could not write cache key {}", key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            client.del(key);
        } catch (RuntimeException e) {
            log.warn("Could not delete cache key {}", key, e);
        }
    }
}