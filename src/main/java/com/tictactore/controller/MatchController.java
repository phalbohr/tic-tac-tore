package com.tictactore.controller;

import com.tictactore.api.MatchApi;
import com.tictactore.dto.MatchRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @purpose REST Controller for managing 2v2 foosball matches.
 */
@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Slf4j
public class MatchController implements MatchApi {

    private final MatchService matchService;

    @Override
    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody MatchRequest request) {
        log.info("Received request to create a new match. Participants: {}, {}, {}, {}", 
            request.teamAAttackerId(), request.teamADefenderId(), 
            request.teamBAttackerId(), request.teamBDefenderId());
        
        var createdMatch = matchService.createMatch(request);
        
        log.info("Successfully created match with ID: {}", createdMatch.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMatch);
    }

    @Override
    @GetMapping("/pending")
    public ResponseEntity<List<MatchResponse>> getPendingMatches() {
        log.info("Received request to get pending matches for current user");
        var pendingMatches = matchService.getPendingMatchesForCurrentUser();
        return ResponseEntity.ok(pendingMatches);
    }

    @Override
    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approveMatch(@PathVariable UUID id) {
        log.info("Received request to approve match with ID: {}", id);
        matchService.approveMatch(id);
        log.info("Successfully processed approval for match with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> rejectMatch(@PathVariable UUID id) {
        log.info("Received request to reject match with ID: {}", id);
        matchService.rejectMatch(id);
        log.info("Successfully processed rejection for match with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
