package com.algoverse.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algoverse.platform.entity.Problem;
import com.algoverse.platform.service.ProblemUploadingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/problems")
@Slf4j
@RequiredArgsConstructor
public class ProblemUploadingController {

    private final ProblemUploadingService problemUploadingService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProblem(@RequestBody List<Problem> problems) {
        if (CollectionUtils.isEmpty(problems)) {
            return ResponseEntity.badRequest().body("No problems to upload");
        }
        log.info("Received {} problems for upload", problems.size());
        problemUploadingService.uploadProblems(problems);
        return ResponseEntity.ok("Problems uploaded successfully");
    }
}
