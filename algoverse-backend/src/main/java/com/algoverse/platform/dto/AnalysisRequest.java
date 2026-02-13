package com.algoverse.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalysisRequest {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("total_solved")
    private int totalSolved;

    @JsonProperty("easy_solved")
    private int easySolved;

    @JsonProperty("medium_solved")
    private int mediumSolved;

    @JsonProperty("hard_solved")
    private int hardSolved;

    @JsonProperty("topic_stats")
    private Map<String, Double> topicStats;

    @JsonProperty("recent_problems")
    private List<String> recentProblems;
}
