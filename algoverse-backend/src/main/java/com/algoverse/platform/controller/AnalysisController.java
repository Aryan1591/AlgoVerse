package com.algoverse.platform.controller;

import com.algoverse.platform.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AnalysisService analysisService;

    /**
     * Generates a performance analysis report for the user.
     * This endpoint should be rate-limited to avoid excessive AI API usage.
     * 
     * @param userId The ID of the user requesting analysis.
     * @return The analysis report in Markdown format.
     */
    @GetMapping("/generate")
    public ResponseEntity<String> generateAnalysis(Principal principal) {
        String userId = (principal != null) ? principal.getName() : null;
        log.info("Received analysis request for user: {}", userId);
        String report = analysisService.generatePerformanceAnalysis(userId);
        return ResponseEntity.ok(report);
    }
}
