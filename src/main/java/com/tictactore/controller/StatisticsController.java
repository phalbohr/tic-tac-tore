package com.tictactore.controller;

import com.tictactore.dto.LeaderboardEntry;
import com.tictactore.dto.PageResponse;
import com.tictactore.dto.PlayerStatsResponse;
import com.tictactore.model.User;
import com.tictactore.service.LeaderboardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Validated
public class StatisticsController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/leaderboard")
    public ResponseEntity<PageResponse<LeaderboardEntry>> getLeaderboard(
            @RequestParam(required = false) @Pattern(regexp = "OVERALL|ATTACKER|DEFENDER", message = "type must be OVERALL, ATTACKER, or DEFENDER") String type,
            @RequestParam(required = false) @Pattern(regexp = "WEEKLY|MONTHLY|YEARLY|ALL_TIME", message = "period must be WEEKLY, MONTHLY, YEARLY, or ALL_TIME") String period,
            @RequestParam(defaultValue = "5") @Min(0) int minMatches,
            @RequestParam(required = false) @Pattern(regexp = "STANDARD|RANDOM", message = "matchFormat must be STANDARD or RANDOM") String matchFormat,
            @RequestParam(required = false) @Pattern(regexp = "1v1|2v2", message = "matchType must be 1v1 or 2v2") String matchType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(type, period, minMatches, matchType, matchFormat, page, size));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlayerStatsResponse> getPersonalStats(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(leaderboardService.getPersonalStats(principal.getId()));
    }
}
