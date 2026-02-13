package com.algoverse.platform.controller;

import com.algoverse.platform.dto.ProblemDto;
import com.algoverse.platform.entity.Category;
import com.algoverse.platform.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<Page<ProblemDto>> getProblems(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Set<String> topics,
            @RequestParam(required = false) Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        String userId = (principal != null) ? principal.getName() : null;
        // TODO : to decide how to get userId

        return ResponseEntity.ok(problemService.getProblems(title, topics, category, page, size, userId));
    }
}
