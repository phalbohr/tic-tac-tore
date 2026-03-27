package com.tictactore.api;

import com.tictactore.dto.statistics.H2HParams;
import com.tictactore.dto.statistics.H2HStatsResponse;
import com.tictactore.dto.statistics.LeaderboardEntryResponse;
import com.tictactore.dto.statistics.LeaderboardParams;
import com.tictactore.dto.statistics.PlayerStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Statistics", description = "Endpoints for player rankings and performance analytics")
public interface StatisticsApi {

    @Operation(summary = "Get global leaderboard", operationId = "getLeaderboard")
    ResponseEntity<Page<LeaderboardEntryResponse>> getLeaderboard(
            @ParameterObject LeaderboardParams params,
            @Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Get personal statistics", operationId = "getPersonalStats")
    ResponseEntity<PlayerStatsResponse> getPersonalStats(
            @Parameter(description = "Time period for statistics") String period);

    @Operation(summary = "Get Head-to-Head statistics", operationId = "getH2HStats")
    ResponseEntity<Page<H2HStatsResponse>> getH2HStats(
            @ParameterObject H2HParams params,
            @Parameter(hidden = true) Pageable pageable);
}
