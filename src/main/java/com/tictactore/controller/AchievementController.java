package com.tictactore.controller;

import com.tictactore.dto.PlayerAchievementsSummaryResponse;
import com.tictactore.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping("/{id}/achievements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlayerAchievementsSummaryResponse> getPlayerAchievements(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal Object principal
    ) {
        PlayerAchievementsSummaryResponse response = achievementService.getPlayerAchievements(id);
        return ResponseEntity.ok(response);
    }
}
