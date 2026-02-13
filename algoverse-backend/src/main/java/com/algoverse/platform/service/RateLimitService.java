package com.algoverse.platform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private static final int WINDOW_DURATION_SECONDS = 60; // 1 Minute

    private static final int AI_GEN_LIMIT = 3;
    private static final int AI_GEN_WINDOW = 86400; // 1 Day

    private static final int AI_ANALYSIS_LIMIT = 1;
    private static final int AI_ANALYSIS_WINDOW = 86400; // 1 Day

    // Implements a "Fixed Window" Rate Limiting strategy using Redis.
    // Logic:
    // 1. We create a key like "rate_limit:user123".
    // 2. We increment it. If it doesn't exist, Redis starts at 0.
    // 3. On the FIRST increment, we set an expiration (e.g., 60 seconds).
    // 4. If the count exceeds the limit within that window, we deny the request.
    // 5. When the key expires, the window resets automatically.

    public boolean tryConsume(String key, int promptTokens, int limit, int durationSeconds) {
        String redisKey = "rate_limit:" + key;

        // Simple Fixed Window Strategy
        // Increment the counter
        Long count = redisTemplate.opsForValue().increment(redisKey, promptTokens);

        // If it's the first request (count == promptTokens), set expiration
        if (count != null && count == promptTokens) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(durationSeconds));
        }

        return count != null && count <= limit;
    }

    // Adapter for the interceptor to use
    // Since we are moving away from Bucket4j objects to direct Redis logic for
    // simplicity without extra deps
    // and true distributed nature.

    public boolean allowRequest(String key) {
        return tryConsume(key, 1, MAX_REQUESTS_PER_MINUTE, WINDOW_DURATION_SECONDS);
    }

    public boolean allowAiGeneration(String key) {
        return tryConsume("ai_gen:" + key, 1, AI_GEN_LIMIT, AI_GEN_WINDOW);
    }

    public boolean allowAiAnalysis(String key) {
        return tryConsume("ai_analysis:" + key, 1, AI_ANALYSIS_LIMIT, AI_ANALYSIS_WINDOW);
    }
}
