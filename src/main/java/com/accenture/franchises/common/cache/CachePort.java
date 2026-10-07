package com.accenture.franchises.common.cache;

import java.util.Optional;

public interface CachePort {

    Optional<String> get(String key);

    void put(String key, String value, long ttlSeconds);

    void delete(String key);
}