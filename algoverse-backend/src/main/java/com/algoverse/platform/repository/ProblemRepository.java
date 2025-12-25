package com.algoverse.platform.repository;

import com.algoverse.platform.entity.Problem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Repository
public class ProblemRepository {

    private final MongoTemplate mongoTemplate;
    public List<Problem> getAllProblems() {
        return mongoTemplate.findAll(Problem.class);
    }
}
