package com.algoverse.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algoverse.platform.dto.LeaderBoardResponse;
import com.algoverse.platform.service.LeaderBoardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {
    
    private final LeaderBoardService leaderBoardService;
    
    @GetMapping
    public ResponseEntity<List<LeaderBoardResponse>> getLeaderBoard() {
        //TODO : get authId from security context
        return ResponseEntity.ok(leaderBoardService.getLeaderBoard(""));
    }
}
