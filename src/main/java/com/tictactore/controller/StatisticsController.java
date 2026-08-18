package com.tictactore.controller;

import com.tictactore.dto.LeaderboardEntry;
import com.tictactore.dto.PageResponse;
import com.tictactore.dto.PagedResponse;
import com.tictactore.dto.PlayerStatsResponse;
import com.tictactore.dto.TeamPairStatsResponse;
import com.tictactore.dto.TimePeriod;
import com.tictactore.model.User;
import com.tictactore.service.LeaderboardService;
import com.tictactore.service.StatisticsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Validated
public class StatisticsController {

    private final LeaderboardService leaderboardService;
    private final StatisticsService statisticsService;

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

    @GetMapping("/team-pairs")
    public ResponseEntity<PagedResponse<TeamPairStatsResponse>> getTeamPairStats(
            @RequestParam(required = false) UUID playerId,
            @RequestParam(required = false, defaultValue = "ALL_TIME") TimePeriod period,
            @RequestParam(required = false) UUID ruleConfigId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "1") int minMatches
    ) {
        PagedResponse<TeamPairStatsResponse> response = statisticsService.getTeamPairStats(
                playerId,
                period,
                ruleConfigId,
                page,
                size,
                minMatches
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/head-to-head")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.tictactore.dto.H2HStatsResponse> getHeadToHeadStats(
            @AuthenticationPrincipal Object principal,
            @RequestParam UUID opponentId,
            @RequestParam(required = false, defaultValue = "ALL_TIME") TimePeriod period,
            @RequestParam(required = false) UUID ruleConfigId,
            @RequestParam(required = false) @Pattern(regexp = "1v1|2v2", message = "matchType must be 1v1 or 2v2") String matchType
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        UUID playerId = resolveUserId(principal);
        if (playerId == null) {
            return ResponseEntity.status(401).build();
        }
        if (playerId.equals(opponentId)) {
            return ResponseEntity.badRequest().build();
        }
        com.tictactore.dto.H2HStatsResponse response = statisticsService.getHeadToHeadStats(
                playerId,
                opponentId,
                period,
                ruleConfigId,
                matchType
        );
        return ResponseEntity.ok(response);
    }

    private UUID resolveUserId(Object principal) {
        if (principal instanceof User user) {
            return user.getId();
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            try {
                return UUID.fromString(userDetails.getUsername());
            } catch (Exception ignored) {
                return UUID.nameUUIDFromBytes(userDetails.getUsername().getBytes());
            }
        }
        if (principal instanceof String str) {
            try {
                return UUID.fromString(str);
            } catch (Exception ignored) {
                return UUID.nameUUIDFromBytes(str.getBytes());
            }
        }
        return null;
    }
}
