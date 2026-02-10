package com.algoverse.platform.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.algoverse.platform.entity.Problem;
import com.algoverse.platform.repository.ProblemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProblemUploadingService {

    private final ProblemRepository problemRepository;
    
    public void uploadProblems(List<Problem> problems) {
        problemRepository.saveAll(problems);
    }
}
