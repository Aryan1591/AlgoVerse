package com.algoverse.platform.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.algoverse.platform.dto.LeaderBoardResponse;
import com.algoverse.platform.entity.Category;
import com.algoverse.platform.entity.Stats;
import com.algoverse.platform.entity.UserProfile;
import com.algoverse.platform.exception.BatchNotFoundException;
import com.algoverse.platform.exception.UserNotFoundException;
import com.algoverse.platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaderBoardService {
    
    private final UserRepository userRepository;
    
    public List<LeaderBoardResponse> getLeaderBoard(String authId) {
        UserProfile currentProfile = userRepository.findByAuthId(authId);
        if (currentProfile == null) {
            throw new UserNotFoundException("User not found");
        }
        if (currentProfile.getBatchId() == null) {
            throw new BatchNotFoundException("Batch not found");
        }
        List<UserProfile> userProfiles = userRepository.findByBatchId(currentProfile.getBatchId());
        return aggregateStats(userProfiles);
    }

    private List<LeaderBoardResponse> aggregateStats(List<UserProfile> userProfiles) {
        Collections.sort(userProfiles, new Comparator<>(){
              public int compare(UserProfile user1, UserProfile user2) {
                  Stats stats1 = user1.getStats();
                  Stats stats2 = user2.getStats();
                  int score1 = calculateScore(stats1);
                  int score2 = calculateScore(stats2);
                  if (score1 != score2) {
                      return Integer.compare(score2, score1); 
                  }
                  if (!stats1.getHardSolved().equals(stats2.getHardSolved())) {
                      return Integer.compare(
                        stats2.getHardSolved(),
                        stats1.getHardSolved()
                      );
                  }
                  if (!stats1.getMediumSolved().equals(stats2.getMediumSolved())) {
                      return Integer.compare(
                        stats2.getMediumSolved(),
                        stats1.getMediumSolved()
                      );
                  }
                  return Integer.compare(
                    stats2.getTotalSolved(),
                    stats1.getTotalSolved()
                  );
              }
        });
        List<LeaderBoardResponse> response = new ArrayList<>();
        int rank = 1;

        for (UserProfile profile : userProfiles) {
            response.add(new LeaderBoardResponse(
                rank++,
                calculateScore(profile.getStats()),
                profile.getDisplayName(),
                profile.getLeetCodeUserName(),
                profile.getStats()
            ));
        }
        return response;
    }

    private int calculateScore(Stats stats) {
        return stats.getEasySolved() * Category.EASY.getScore()
             + stats.getMediumSolved() * Category.MEDIUM.getScore()
             + stats.getHardSolved() * Category.HARD.getScore();
    } 
}
