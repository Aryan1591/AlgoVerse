package com.algoverse.platform.repository;

import com.algoverse.platform.entity.Problem;
import com.algoverse.platform.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class ProblemRepository {

    private static final String PROBLEMS_CACHE_KEY = "problems:all";
    private static final Duration CACHE_TTL = Duration.ofHours(12);

    private final MongoTemplate mongoTemplate;
    private final RedisCacheService redisCacheService;

    @SuppressWarnings("unchecked")
    public List<Problem> getAllProblems() {
        // Try to get from cache first
        List<Problem> cachedProblems = redisCacheService.get(PROBLEMS_CACHE_KEY, List.class);

        if (cachedProblems != null) {
            log.debug("Fetching problems from Redis Cache");
            return cachedProblems;
        }

        // Cache miss - fetch from database
        log.info("Cache miss - fetching all problems from Mongo");
        List<Problem> problems = mongoTemplate.findAll(Problem.class);

        // Store in cache
        redisCacheService.set(PROBLEMS_CACHE_KEY, problems, CACHE_TTL);
        log.info("Cached {} problems with TTL of {} hours", problems.size(), CACHE_TTL.toHours());

        return problems;
    }

    public void saveAll(List<Problem> problems) {
        log.info("Saving {} problems and evicting cache", problems.size());
        mongoTemplate.insertAll(problems);

        // Evict cache after saving
        redisCacheService.delete(PROBLEMS_CACHE_KEY);
        log.info("Cache evicted for key: {}", PROBLEMS_CACHE_KEY);
    }
}
