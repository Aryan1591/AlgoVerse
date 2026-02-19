package com.algoverse.platform.repository;

import com.algoverse.platform.entity.Stats;
import com.algoverse.platform.entity.UserProfile;
import com.algoverse.platform.utils.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Repository
public class UserRepository {

    private final MongoTemplate mongoTemplate;

    public void updateStats(String userId, Stats stats) {

        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(userId));

        Update update = new Update()
                .set("stats", stats)
                .set("updatedAt", Instant.now());

        mongoTemplate.updateFirst(query, update, UserProfile.class);

        log.info("Updated stats for user {}", userId);
    }

    public void incrementStats(String userId, int easyDiff, int mediumDiff, int hardDiff) {
        Query query = new Query(Criteria.where("id").is(userId));
        Update update = new Update()
                .inc("stats.easySolved", easyDiff)
                .inc("stats.mediumSolved", mediumDiff)
                .inc("stats.hardSolved", hardDiff)
                .inc("stats.totalSolved", easyDiff + mediumDiff + hardDiff)
                .set("updatedAt", Instant.now());

        mongoTemplate.updateFirst(query, update, UserProfile.class);
        log.info("Incremented stats for user {}: +E{}, +M{}, +H{}", userId, easyDiff, mediumDiff, hardDiff);
    }

    public List<UserProfile> findActiveUserProfile() {
        Query query = new Query();
        query.addCriteria(Criteria.where("active").is(true));
        return mongoTemplate.find(query, UserProfile.class);
    }

    public UserProfile findByLeetCodeUserName(String leetCodeUserName) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Constants.LEETCODE_USERNAME).is(leetCodeUserName));
        return mongoTemplate.findOne(query, UserProfile.class);
    }

    public List<UserProfile> findByBatchId(String batchId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Constants.BATCH_ID).is(batchId));
        return mongoTemplate.find(query, UserProfile.class);
    }

    public UserProfile findByAuthId(String authId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Constants.AUTH_ID).is(authId));
        return mongoTemplate.findOne(query, UserProfile.class);
    }
}
