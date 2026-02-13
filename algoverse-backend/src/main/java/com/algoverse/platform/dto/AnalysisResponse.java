package com.algoverse.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AnalysisResponse {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("analysis_report")
    private String analysisReport;
}
