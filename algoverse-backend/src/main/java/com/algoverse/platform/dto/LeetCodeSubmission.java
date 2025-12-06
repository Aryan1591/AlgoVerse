package com.algoverse.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeetCodeSubmission {
    private String title;
    private String titleSlug;
    private String timestamp;
    private String statusDisplay;
    private String lang;
}
