package com.tictactore.controller;

import com.tictactore.dto.ChallengeActionResponse;
import com.tictactore.dto.ChallengeResponse;
import com.tictactore.dto.CreateChallengeRequest;
import com.tictactore.model.User;
import com.tictactore.service.ChallengeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    public ResponseEntity<ChallengeResponse> createChallenge(
            @Valid @RequestBody CreateChallengeRequest request,
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ChallengeResponse response = challengeService.createChallenge(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<ChallengeResponse>> getIncomingChallenges(
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ChallengeResponse> response = challengeService.getIncomingChallenges(principal.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/outgoing")
    public ResponseEntity<List<ChallengeResponse>> getOutgoingChallenges(
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ChallengeResponse> response = challengeService.getOutgoingChallenges(principal.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeResponse> getChallengeById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ChallengeResponse response = challengeService.getChallengeById(id, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ChallengeActionResponse> acceptChallenge(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ChallengeActionResponse response = challengeService.acceptChallenge(id, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<ChallengeActionResponse> declineChallenge(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ChallengeActionResponse response = challengeService.declineChallenge(id, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ChallengeActionResponse> cancelChallenge(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ChallengeActionResponse response = challengeService.cancelChallenge(id, principal.getId());
        return ResponseEntity.ok(response);
    }
}
