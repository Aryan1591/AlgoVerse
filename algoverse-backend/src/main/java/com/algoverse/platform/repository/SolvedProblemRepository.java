package com.algoverse.platform.repository;

import com.algoverse.platform.entity.SolvedProblem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class SolvedProblemRepository {

    private final MongoTemplate mongoTemplate;

    public void bulkUpsertProblems(String userId, List<SolvedProblem> problems) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, SolvedProblem.class);

        Instant now = Instant.now();

        for (SolvedProblem p : problems) {
            Query q = Query.query(Criteria.where("userId").is(userId)
                    .and("problemName").is(p.getProblemName()));

            Update u = new Update()
                    .set("userId", p.getUserId())
                    .set("problemSlug", p.getProblemSlug())
                    .set("problemName", p.getProblemName())
                    .set("language", p.getLanguage())
                    .set("solvedAt", p.getSolvedAt())
                    .set("updatedAt", now)
                    .setOnInsert("createdAt", now);

            bulkOps.upsert(q, u);
        }

        if (!problems.isEmpty()) {
            bulkOps.execute();
            log.info("Bulk upserted {} problems for user {}", problems.size(), userId);
        }
    }

    public List<SolvedProblem> getSolvedProblemsFromUser(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        return mongoTemplate.find(query, SolvedProblem.class);
    }
}
