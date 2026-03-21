package com.tictactore.api;

import com.tictactore.dto.statistics.H2HStatsResponse;
import com.tictactore.dto.statistics.LeaderboardEntryResponse;
import com.tictactore.dto.statistics.LeaderboardType;
import com.tictactore.dto.statistics.PlayerStatsResponse;
import com.tictactore.dto.statistics.TimePeriod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Statistics", description = "Endpoints for player rankings and performance analytics")
public interface StatisticsApi {

    @Operation(summary = "Get global leaderboard", operationId = "getLeaderboard")
    @GetMapping("/leaderboard")
    ResponseEntity<Page<LeaderboardEntryResponse>> getLeaderboard(
            @RequestParam(required = false, defaultValue = "OVERALL") LeaderboardType type,
            @RequestParam(required = false, defaultValue = "ALL_TIME") TimePeriod period,
            @RequestParam(required = false, defaultValue = "0") Integer minMatches,
            @Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Get personal statistics", operationId = "getPersonalStats")
    @GetMapping("/me")
    ResponseEntity<PlayerStatsResponse> getPersonalStats(
            @RequestParam(required = false, defaultValue = "ALL_TIME") TimePeriod period);

    @Operation(summary = "Get Head-to-Head statistics", operationId = "getH2HStats")
    @GetMapping("/h2h")
    ResponseEntity<Page<H2HStatsResponse>> getH2HStats(
            @RequestParam(required = false, defaultValue = "ALL_TIME") TimePeriod period,
            @Parameter(hidden = true) Pageable pageable);
}
