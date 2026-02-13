package com.algoverse.platform.repository;

import com.algoverse.platform.entity.Problem;
import com.algoverse.platform.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Duration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.algoverse.platform.entity.Category;

@RequiredArgsConstructor
@Slf4j
@Repository
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

    public Page<Problem> findProblems(String title, Set<String> topics, Category category, Pageable pageable) {
        List<Criteria> criteria = new ArrayList<>();

        if (StringUtils.hasText(title)) {
            // Case-insensitive regex search for title containing the query string
            criteria.add(Criteria.where("title").regex(Pattern.quote(title), "i"));
        }

        if (topics != null && !topics.isEmpty()) {
            criteria.add(Criteria.where("topics").all(topics));
        }

        if (category != null) {
            criteria.add(Criteria.where("category").is(category));
        }

        Query query = new Query();
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        long count = mongoTemplate.count(query, Problem.class);

        query.with(pageable);
        List<Problem> problems = mongoTemplate.find(query, Problem.class);

        return new PageImpl<>(problems, pageable, count);
    }
}
