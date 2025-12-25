package com.algoverse.platform.repository;

import com.algoverse.platform.entity.Stats;
import com.algoverse.platform.entity.UserProfile;
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

    public List<UserProfile> findActiveUserProfile() {
        Query query = new Query();
        query.addCriteria(Criteria.where("active").is(true));
        return mongoTemplate.find(query, UserProfile.class);
    }
}
