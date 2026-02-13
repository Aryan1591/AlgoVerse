package com.algoverse.platform.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "solved_problems")
@Data
@CompoundIndex(def = "{'userId': 1, 'problemId': 1}", name = "user_problem_idx", unique = true)
public class SolvedProblem {

    @Id
    private String id;
    private String userId;
    private String problemSlug;
    private String problemName;
    private String problemId;
    private Instant solvedAt;
    private String language;
    private Instant createdAt;
    private Instant updatedAt;
}
