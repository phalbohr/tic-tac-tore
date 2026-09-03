package com.tictactore.controller;

import com.tictactore.dto.TournamentBracketResponse;
import com.tictactore.dto.TournamentMatchResponse;
import com.tictactore.dto.TournamentResponse;
import com.tictactore.model.User;
import com.tictactore.service.tournament.TournamentLifecycleService;
import com.tictactore.service.tournament.TournamentMatchQueryService;
import com.tictactore.service.tournament.TournamentMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentLifecycleService tournamentLifecycleService;
    private final TournamentMatchQueryService tournamentMatchQueryService;
    private final TournamentMatchService tournamentMatchService;

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

    @PostMapping("/{id}/matches/{matchId}/start")
    public ResponseEntity<TournamentMatchResponse> startMatch(
            @PathVariable UUID id,
            @PathVariable UUID matchId,
            @AuthenticationPrincipal Object principal,
            Principal authPrincipal
    ) {
        UUID currentUserId = resolveUserId(principal, authPrincipal);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TournamentMatchResponse response = tournamentMatchService.startMatch(id, matchId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/matches/{matchId}/cancel")
    public ResponseEntity<TournamentMatchResponse> cancelMatch(
            @PathVariable UUID id,
            @PathVariable UUID matchId,
            @AuthenticationPrincipal Object principal,
            Principal authPrincipal
    ) {
        UUID currentUserId = resolveUserId(principal, authPrincipal);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TournamentMatchResponse response = tournamentMatchService.cancelMatch(id, matchId, currentUserId);
        return ResponseEntity.ok(response);
    }

    private UUID resolveUserId(Object principal, Principal authPrincipal) {
        if (principal instanceof User user) {
            return user.getId();
        }
        if (authPrincipal != null && authPrincipal.getName() != null) {
            try {
                return UUID.fromString(authPrincipal.getName());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}
