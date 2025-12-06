package com.algoverse.platform.service;

import com.algoverse.platform.dto.LeetCodeSubmission;
import com.algoverse.platform.dto.LeetCodeSubmissionResponse;
import com.algoverse.platform.entity.Category;
import com.algoverse.platform.entity.SolvedProblem;
import com.algoverse.platform.entity.Stats;
import com.algoverse.platform.entity.UserProfile;
import com.algoverse.platform.repository.SolvedProblemRepository;
import com.algoverse.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void syncUserProblems(UserProfile userProfile, Map<String, Category> problemScoreMap) {
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

            List<SolvedProblem> problems = mapToSolvedProblems(userProfile.getId(), response.getSubmission(), problemScoreMap);
            if (!problems.isEmpty()) {
                solvedProblemRepository.bulkUpsertProblems(userProfile.getId(), problems);
            } else {
                log.info("No problems to upsert for user {}", leetCodeUsername);
            }
            // Update Stats , call DB to get all solved problems of this user
            //TODO : Optimize it
            updateStats(userProfile.getId(), problemScoreMap);

        } catch (Exception ex) {
            log.error("Failed to sync user {}: {}", userProfile.getId(), ex.getMessage(), ex);
        }
    }

    private List<SolvedProblem> mapToSolvedProblems(String userId, List<LeetCodeSubmission> submissions, Map<String, Category> problemScoreMap) {
        List<SolvedProblem> list = new ArrayList<>();
        for (LeetCodeSubmission s : submissions) {
            if (problemScoreMap.containsKey(s.getTitle())) {
                SolvedProblem sp = new SolvedProblem();
                sp.setUserId(userId);
                sp.setProblemName(s.getTitle());
                sp.setProblemSlug(s.getTitleSlug());
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
        if (StringUtils.isEmpty(timeStamp)) return null;
        try {
            long seconds = Long.parseLong(timeStamp);
            return Instant.ofEpochSecond(seconds);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse timestamp: {}", timeStamp);
            return null;
        }
    }

    protected void updateStats(String userId, Map<String, Category> problemScoreMap) {
        List<SolvedProblem> userSolvedProblems = solvedProblemRepository.getSolvedProblemsFromUser(userId);
        int[] categoryCount = new int[3];
        for (SolvedProblem solvedProblem : userSolvedProblems) {
            Category category = problemScoreMap.get(solvedProblem.getProblemName());
            switch (category) {
                case EASY -> categoryCount[0]++;
                case MEDIUM -> categoryCount[1]++;
                case HARD -> categoryCount[2]++;
            }
        }
        Stats stats = new Stats(categoryCount[0], categoryCount[1], categoryCount[2], categoryCount[0] + categoryCount[1] +
                categoryCount[2]);
        userRepository.updateStats(userId, stats);
    }

}
