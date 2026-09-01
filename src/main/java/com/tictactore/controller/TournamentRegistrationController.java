package com.tictactore.controller;

import com.tictactore.dto.MyRegistrationStatusResponse;
import com.tictactore.dto.RegisterTournamentRequest;
import com.tictactore.dto.TournamentRegistrationResponse;
import com.tictactore.model.RegistrationStatus;
import com.tictactore.model.User;
import com.tictactore.service.TournamentRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tournaments")
@RequiredArgsConstructor
public class TournamentRegistrationController {

    private final TournamentRegistrationService registrationService;

    @PostMapping("/{tournamentId}/registrations")
    public ResponseEntity<TournamentRegistrationResponse> register(
            @PathVariable UUID tournamentId,
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody RegisterTournamentRequest request
    ) {
        var response = registrationService.register(tournamentId, principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{tournamentId}/registrations")
    public ResponseEntity<List<TournamentRegistrationResponse>> listRegistrations(
            @PathVariable UUID tournamentId,
            @RequestParam(required = false) RegistrationStatus status
    ) {
        var registrations = registrationService.listRegistrations(tournamentId, status);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/{tournamentId}/registrations/my")
    public ResponseEntity<MyRegistrationStatusResponse> getMyRegistrationStatus(
            @PathVariable UUID tournamentId,
            @AuthenticationPrincipal User principal
    ) {
        var status = registrationService.getMyRegistrationStatus(tournamentId, principal.getId());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/{tournamentId}/registrations/{registrationId}/accept")
    public ResponseEntity<TournamentRegistrationResponse> acceptInvitation(
            @PathVariable UUID tournamentId,
            @PathVariable UUID registrationId,
            @AuthenticationPrincipal User principal
    ) {
        var response = registrationService.acceptInvitation(tournamentId, registrationId, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{tournamentId}/registrations/{registrationId}/decline")
    public ResponseEntity<TournamentRegistrationResponse> declineInvitation(
            @PathVariable UUID tournamentId,
            @PathVariable UUID registrationId,
            @AuthenticationPrincipal User principal
    ) {
        var response = registrationService.declineInvitation(tournamentId, registrationId, principal.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{tournamentId}/registrations/{registrationId}")
    public ResponseEntity<Void> cancelRegistration(
            @PathVariable UUID tournamentId,
            @PathVariable UUID registrationId,
            @AuthenticationPrincipal User principal
    ) {
        registrationService.cancelRegistration(tournamentId, registrationId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/invitations/pending")
    public ResponseEntity<List<TournamentRegistrationResponse>> getPendingInvitations(
            @AuthenticationPrincipal User principal
    ) {
        var invitations = registrationService.getPendingInvitations(principal.getId());
        return ResponseEntity.ok(invitations);
    }
}
