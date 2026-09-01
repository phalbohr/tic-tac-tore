package com.tictactore.controller;

import com.tictactore.dto.TournamentBracketResponse;
import com.tictactore.dto.TournamentMatchResponse;
import com.tictactore.dto.TournamentResponse;
import com.tictactore.service.tournament.TournamentLifecycleService;
import com.tictactore.service.tournament.TournamentMatchQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentLifecycleService tournamentLifecycleService;
    private final TournamentMatchQueryService tournamentMatchQueryService;

    @PostMapping("/{id}/start")
    public ResponseEntity<TournamentResponse> startTournament(@PathVariable UUID id) {
        TournamentResponse response = tournamentLifecycleService.startTournament(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/bracket")
    public ResponseEntity<TournamentBracketResponse> getTournamentBracket(@PathVariable UUID id) {
        TournamentBracketResponse response = tournamentMatchQueryService.getTournamentBracket(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<List<TournamentMatchResponse>> getTournamentMatches(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer round
    ) {
        List<TournamentMatchResponse> matches = tournamentMatchQueryService.getTournamentMatches(id, round);
        return ResponseEntity.ok(matches);
    }
}
