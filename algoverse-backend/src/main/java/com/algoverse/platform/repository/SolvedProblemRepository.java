package com.algoverse.platform.repository;

import com.algoverse.platform.entity.SolvedProblem;
import com.algoverse.platform.utils.Constants;
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
            Query q = Query.query(Criteria.where(Constants.USER_ID).is(userId)
                    .and(Constants.PROBLEM_ID).is(p.getProblemId()));

            Update u = new Update()
                    .set(Constants.USER_ID, p.getUserId())
                    .set(Constants.PROBLEM_SLUG, p.getProblemSlug())
                    .set(Constants.PROBLEM_NAME, p.getProblemName())
                    .set(Constants.PROBLEM_ID, p.getProblemId())
                    .set(Constants.LANGUAGE, p.getLanguage())
                    .set(Constants.SOLVED_AT, p.getSolvedAt())
                    .set(Constants.UPDATED_AT, now)
                    .setOnInsert(Constants.CREATED_AT, now);

            bulkOps.upsert(q, u);
        }

        if (!problems.isEmpty()) {
            bulkOps.execute();
            log.info("Bulk upserted {} problems for user {}", problems.size(), userId);
        }
    }

    public List<SolvedProblem> getSolvedProblemsFromUser(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Constants.USER_ID).is(userId));
        return mongoTemplate.find(query, SolvedProblem.class);
    }

    public List<SolvedProblem> findSolvedProblemsByProblemIds(String userId, List<String> problemIds) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Constants.USER_ID).is(userId)
                .and(Constants.PROBLEM_ID).in(problemIds));
        return mongoTemplate.find(query, SolvedProblem.class);
    }
}
