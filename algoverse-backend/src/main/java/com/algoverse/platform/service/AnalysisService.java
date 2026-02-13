package com.algoverse.platform.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.algoverse.platform.dto.AnalysisRequest;
import com.algoverse.platform.dto.AnalysisResponse;
import com.algoverse.platform.entity.Problem;
import com.algoverse.platform.entity.SolvedProblem;
import com.algoverse.platform.exception.UserNotFoundException;
import com.algoverse.platform.repository.ProblemRepository;
import com.algoverse.platform.repository.SolvedProblemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final SolvedProblemRepository solvedProblemRepository;
    private final ProblemRepository problemRepository;
    private final RestTemplate restTemplate;

    @Value("${algoverse.ai.url:http://localhost:8000}")
    private String aiServiceUrl;

    public String generatePerformanceAnalysis(String userId) {
        if (StringUtils.isEmpty(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        // 1. Fetch user's solved problems
        List<SolvedProblem> solvedProblems = solvedProblemRepository.getSolvedProblemsFromUser(userId);

        if (solvedProblems.isEmpty()) {
            return "No solved problems found. Start solving problems to get an analysis!";
        }

        // 2. Fetch all problems to get metadata (difficulty, topics)
        // Ideally we should cache this map or fetch only relevant problems, but
        // getAllProblems is already cached.
        List<Problem> allProblems = problemRepository.getAllProblems();
        Map<String, Problem> problemMap = allProblems.stream()
                .collect(Collectors.toMap(Problem::getId, p -> p));

        // 3. Calculate Stats
        int easy = 0;
        int medium = 0;
        int hard = 0;
        Map<String, Integer> topicCounts = new HashMap<>(); // Topic -> Solved Count

        for (SolvedProblem sp : solvedProblems) {
            Problem p = problemMap.get(sp.getProblemId());
            if (p != null) {
                // Difficulty
                if (p.getCategory() != null) {
                    switch (p.getCategory()) {
                        case EASY -> easy++;
                        case MEDIUM -> medium++;
                        case HARD -> hard++;
                    }
                }

                // Topics
                if (p.getTopics() != null) {
                    for (String topic : p.getTopics()) {
                        topicCounts.merge(topic, 1, Integer::sum);
                    }
                }
            }
        }

        // Convert Topic Counts to percentages or raw values for AI
        // Let's send raw counts and let AI interpret, or normalize.
        // Normalizing vs total problems in that topic is better context.
        Map<String, Integer> totalTopicCounts = new HashMap<>();
        for (Problem p : allProblems) {
            if (p.getTopics() != null) {
                for (String topic : p.getTopics()) {
                    totalTopicCounts.merge(topic, 1, Integer::sum);
                }
            }
        }

        Map<String, Double> topicStats = new HashMap<>();
        for (Map.Entry<String, Integer> entry : topicCounts.entrySet()) {
            String topic = entry.getKey();
            int solved = entry.getValue();
            int total = totalTopicCounts.getOrDefault(topic, 1); // avoid /0
            topicStats.put(topic, (double) solved / total);
        }

        // Recent problems (last 5)
        List<String> recentProblems = solvedProblems.stream()
                .filter(sp -> sp.getSolvedAt() != null)
                .sorted((a, b) -> b.getSolvedAt().compareTo(a.getSolvedAt()))
                .limit(5)
                .map(SolvedProblem::getProblemName)
                .collect(Collectors.toList());

        // 4. Call AI Service
        AnalysisRequest request = AnalysisRequest.builder()
                .userId(userId)
                .totalSolved(solvedProblems.size())
                .easySolved(easy)
                .mediumSolved(medium)
                .hardSolved(hard)
                .topicStats(topicStats)
                .recentProblems(recentProblems)
                .build();

        String url = aiServiceUrl + "/analyze/performance";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AnalysisRequest> entity = new HttpEntity<>(request, headers);

            AnalysisResponse response = restTemplate.postForObject(url, entity, AnalysisResponse.class);
            return response != null ? response.getAnalysisReport() : "Failed to generate report.";

        } catch (Exception e) {
            log.error("Error calling AI service: ", e);
            return "Error generating analysis. Please try again later.";
        }
    }
}
