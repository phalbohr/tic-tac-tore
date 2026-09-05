package com.tictactore.controller;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.MatchConfirmationRequest;
import com.tictactore.dto.MatchRejectionRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.dto.PagedResponse;
import com.tictactore.dto.PendingMatchesResponse;
import com.tictactore.model.User;
import com.tictactore.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(
            @Valid @RequestBody CreateMatchRequest request,
            @AuthenticationPrincipal User principal) {
        CreateMatchRequest finalRequest = request;
        if (principal != null) {
            finalRequest = new CreateMatchRequest(
                    request.idempotencyKey(),
                    principal.getId(),
                    request.teamAAttackerId(),
                    request.teamADefenderId(),
                    request.teamBAttackerId(),
                    request.teamBDefenderId(),
                    request.games(),
                    request.entryMode(),
                    request.matchFormat(),
                    request.tournamentMatchId());
        }

        MatchResponse response = matchService.createMatch(finalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<PendingMatchesResponse> getPendingMatches(@AuthenticationPrincipal User principal) {
        var currentUserId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(matchService.getPendingMatches(currentUserId));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<MatchResponse> confirmMatch(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyHeader,
            @RequestBody(required = false) MatchConfirmationRequest request,
            @AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String idempotencyKey = idempotencyHeader != null ? idempotencyHeader
                : (request != null ? request.idempotencyKey() : null);
        MatchResponse response = matchService.confirmMatch(id, principal.getId(), idempotencyKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<MatchResponse> rejectMatch(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyHeader,
            @Valid @RequestBody MatchRejectionRequest request,
            @AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        MatchResponse response = matchService.rejectMatch(id, principal.getId(), request, idempotencyHeader);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<PagedResponse<MatchResponse>> getMatchHistory(
            @RequestParam(value = "status", required = false, defaultValue = "CONFIRMED") String status,
            @RequestParam(value = "playerId", required = false) UUID playerId,
            @RequestParam(value = "groupId", required = false) UUID groupId,
            @RequestParam(value = "ruleConfigId", required = false) UUID ruleConfigId,
            @RequestParam(value = "matchType", required = false) String matchType,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PagedResponse<MatchResponse> response;
        if (groupId != null) {
            response = matchService.getMatchHistory(
                    principal.getId(), status, playerId, groupId, ruleConfigId, matchType, page, size);
        } else {
            response = matchService.getMatchHistory(
                    principal.getId(), status, playerId, ruleConfigId, matchType, page, size);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        matchService.deleteMatch(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
