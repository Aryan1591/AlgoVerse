package com.algoverse.platform.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Set value without TTL
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Set value with TTL (Duration based)
     */
    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * Set value with TTL (time + unit)
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * Get value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (T) value : null;
    }

    /**
     * Delete key
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * Check if key exists
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Update TTL for existing key
     */
    public boolean expire(String key, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, ttl));
    }
}
