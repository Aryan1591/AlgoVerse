package com.algoverse.platform.service;

import com.algoverse.platform.dto.LeetCodeSubmission;
import com.algoverse.platform.dto.LeetCodeSubmissionResponse;
import com.algoverse.platform.entity.Problem;
import com.algoverse.platform.entity.SolvedProblem;
import com.algoverse.platform.entity.UserProfile;
import com.algoverse.platform.repository.SolvedProblemRepository;
import com.algoverse.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeetCodeSyncService {

    private static final String API_URL = "https://alfa-leetcode-api.onrender.com/%s/acSubmission";

    private final RestTemplate restTemplate;
    private final SolvedProblemRepository solvedProblemRepository;
    private final UserRepository userRepository;

    @Async("taskExecutor")
    public void syncUserProblems(UserProfile userProfile, Map<String, Problem> problemMap) {
        try {
            String leetCodeUsername = userProfile.getLeetCodeUserName();
            if (StringUtils.isEmpty(leetCodeUsername)) {
                log.warn("User {} has no leetCodeUsername — skipping", userProfile.getId());
                return;
            }

            String url = String.format(API_URL, leetCodeUsername);
            LeetCodeSubmissionResponse response = restTemplate.getForObject(url, LeetCodeSubmissionResponse.class);

            if (response == null || response.getSubmission() == null) {
                log.warn("Empty response for user {}", leetCodeUsername);
                return;
            }

            List<SolvedProblem> problems = mapToSolvedProblems(userProfile.getId(), response.getSubmission(),
                    problemMap);

            if (CollectionUtils.isNotEmpty(problems)) {
                // 1. Deduplicate by problemId (keep latest or any)
                // Using a Map to ensure we only have unique problemIds in this batch
                Map<String, SolvedProblem> uniqueBatchProblems = problems.stream()
                        .collect(java.util.stream.Collectors.toMap(
                                SolvedProblem::getProblemId,
                                p -> p,
                                (existing, replacement) -> existing // Keep existing if duplicate in batch
                        ));

                List<String> problemIds = new ArrayList<>(uniqueBatchProblems.keySet());

                // 2. Find which of these are already in DB
                List<SolvedProblem> existingInDb = solvedProblemRepository
                        .findSolvedProblemsByProblemIds(userProfile.getId(), problemIds);
                List<String> existingIds = existingInDb.stream()
                        .map(SolvedProblem::getProblemId)
                        .toList();

                // 3. Identify truly NEW problems
                List<SolvedProblem> newProblems = uniqueBatchProblems.values().stream()
                        .filter(p -> !existingIds.contains(p.getProblemId()))
                        .toList();

                // 4. Bulk Upsert ALL (to update timestamps of existing ones too)
                solvedProblemRepository.bulkUpsertProblems(userProfile.getId(), problems);

                // 5. Increment Stats only for new problems
                if (CollectionUtils.isNotEmpty(newProblems)) {
                    int easy = 0;
                    int medium = 0;
                    int hard = 0;

                    for (SolvedProblem sp : newProblems) {
                        Problem p = problemMap.get(sp.getProblemName());
                        if (p != null && p.getCategory() != null) {
                            switch (p.getCategory()) {
                                case EASY -> easy++;
                                case MEDIUM -> medium++;
                                case HARD -> hard++;
                            }
                        }
                    }

                    if (easy > 0 || medium > 0 || hard > 0) {
                        userRepository.incrementStats(userProfile.getId(), easy, medium, hard);
                    }
                }
            } else {
                log.info("No problems to upsert for user {}", leetCodeUsername);
            }

        } catch (Exception ex) {
            log.error("Failed to sync user {}: {}", userProfile.getId(), ex.getMessage(), ex);
        }
    }

    private List<SolvedProblem> mapToSolvedProblems(String userId, List<LeetCodeSubmission> submissions,
            Map<String, Problem> problemMap) {
        List<SolvedProblem> list = new ArrayList<>();
        for (LeetCodeSubmission s : submissions) {
            if (problemMap.containsKey(s.getTitle())) {
                Problem matchedProblem = problemMap.get(s.getTitle());
                SolvedProblem sp = new SolvedProblem();
                sp.setUserId(userId);
                sp.setProblemName(s.getTitle());
                sp.setProblemSlug(s.getTitleSlug());
                sp.setProblemId(matchedProblem.getId());
                sp.setLanguage(s.getLang());
                sp.setUpdatedAt(Instant.now());
                Instant solvedAt = parseTimeStamp(s.getTimestamp());
                sp.setSolvedAt(solvedAt);
                list.add(sp);
            }
        }
        return list;
    }

    private Instant parseTimeStamp(String timeStamp) {
        if (StringUtils.isEmpty(timeStamp))
            return null;
        try {
            long seconds = Long.parseLong(timeStamp);
            return Instant.ofEpochSecond(seconds);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse timestamp: {}", timeStamp);
            return null;
        }
    }

}
