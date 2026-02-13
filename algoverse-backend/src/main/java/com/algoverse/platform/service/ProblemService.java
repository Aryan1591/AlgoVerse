package com.algoverse.platform.service;

import com.algoverse.platform.dto.ProblemDto;
import com.algoverse.platform.entity.Category;
import com.algoverse.platform.entity.Problem;
import com.algoverse.platform.entity.SolvedProblem;
import com.algoverse.platform.repository.ProblemRepository;
import com.algoverse.platform.repository.SolvedProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final SolvedProblemRepository solvedProblemRepository;

    public Page<ProblemDto> getProblems(String title, Set<String> topics, Category category, int page, int size,
            String userId) {
        Page<Problem> problemsPage = problemRepository.findProblems(title, topics, category,
                PageRequest.of(page, size));

        // Optimize: If no user logged in, or no problems found, return early
        if (!StringUtils.hasText(userId) || problemsPage.isEmpty()) {
            return problemsPage.map(this::mapToDto);
        }

        // Fetch solved status only for the problems in the current page
        List<String> problemIds = problemsPage.getContent().stream()
                .map(Problem::getId)
                .collect(Collectors.toList());

        List<SolvedProblem> solvedProblemsByCurrentUser = solvedProblemRepository.findSolvedProblemsByProblemIds(userId,
                problemIds);
        Set<String> solvedProblemIds = solvedProblemsByCurrentUser.stream()
                .map(SolvedProblem::getProblemId)
                .collect(Collectors.toSet());

        return problemsPage.map(problem -> {
            ProblemDto dto = mapToDto(problem);
            if (problem.getId() != null && solvedProblemIds.contains(problem.getId())) {
                dto.setSolved(true);
            }
            return dto;
        });
    }

    private ProblemDto mapToDto(Problem problem) {
        return ProblemDto.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .titleSlug(problem.getTitleSlug())
                .problemUrl(problem.getProblemUrl())
                .category(problem.getCategory())
                .topics(problem.getTopics())
                .isSolved(false) // Default
                .build();
    }
}
