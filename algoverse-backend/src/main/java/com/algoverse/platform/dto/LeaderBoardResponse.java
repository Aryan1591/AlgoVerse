package com.algoverse.platform.dto;

import com.algoverse.platform.entity.Stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LeaderBoardResponse {
    private Integer rank;
    private Integer totalScore;
    private String displayName;
    private String leetCodeUserName;
    private Stats stats;
}
