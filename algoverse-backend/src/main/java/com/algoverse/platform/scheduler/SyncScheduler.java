package com.algoverse.platform.scheduler;

import com.algoverse.platform.entity.Category;
import com.algoverse.platform.entity.Problem;
import com.algoverse.platform.entity.UserProfile;
import com.algoverse.platform.repository.ProblemRepository;
import com.algoverse.platform.repository.UserRepository;
import com.algoverse.platform.service.LeetCodeSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SyncScheduler {

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final LeetCodeSyncService leetCodeSyncService;

    @Scheduled(cron = "0 0/30 * * * *", zone = "Asia/Kolkata")
    public void runSyncProcess() {
        List<Problem> problemList = problemRepository.getAllProblems();
        List<UserProfile> activeProfiles = userRepository.findActiveUserProfile();
        Map<String, Category> problemScoreMap = buildProblemScoreMap(problemList);
        for (UserProfile userProfile : activeProfiles) {
            leetCodeSyncService.syncUserProblems(userProfile, problemScoreMap);
        }
    }

    private Map<String, Category> buildProblemScoreMap(List<Problem> problems) {
        Map<String, Category> problemScoreMap = new HashMap<>();
        for (Problem problem : problems) {
            problemScoreMap.put(problem.getTitle(), problem.getCategory());
        }
        return problemScoreMap;
    }
}
