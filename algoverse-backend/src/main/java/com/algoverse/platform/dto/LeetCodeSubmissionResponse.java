package com.algoverse.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeetCodeSubmissionResponse {
    private int count;
    private List<LeetCodeSubmission> submission;
}
