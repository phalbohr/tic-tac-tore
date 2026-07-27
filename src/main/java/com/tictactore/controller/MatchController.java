package com.tictactore.controller;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.dto.PendingMatchesResponse;
import com.tictactore.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody CreateMatchRequest request) {
        MatchResponse response = matchService.createMatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<PendingMatchesResponse> getPendingMatches() {
        return ResponseEntity.ok(matchService.getPendingMatches());
    }
}
