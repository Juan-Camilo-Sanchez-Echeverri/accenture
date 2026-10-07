package com.accenture.franchises.common.infrastructure.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.RedisClient;

@Configuration
public class JedisConfig {

    private static final String DEFAULT_USER = "default";

    @Bean(destroyMethod = "close")
    public RedisClient redisClient(
            @Value("${REDIS_HOST}") String host,
            @Value("${REDIS_PORT}") int port,
            @Value("${REDIS_PASSWORD}") String password) {
        if (password == null || password.isBlank()) {
            return RedisClient.create(host, port);
        }
        return RedisClient.create(host, port, DEFAULT_USER, password);
    }
}