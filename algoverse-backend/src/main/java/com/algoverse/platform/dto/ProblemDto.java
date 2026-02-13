package com.algoverse.platform.dto;

import com.algoverse.platform.entity.Category;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class ProblemDto {
    private String id;
    private String title;
    private String problemUrl;
    private String titleSlug;
    private Set<String> topics;
    private Category category;
    private boolean isSolved;
}
