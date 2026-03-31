package com.tictactore.controller;

import com.tictactore.api.StatisticsApi;
import com.tictactore.dto.statistics.*;
import com.tictactore.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController implements StatisticsApi {

    private final StatisticsService statisticsService;

    @Override
    @GetMapping("/leaderboard")
    public ResponseEntity<Page<LeaderboardEntryResponse>> getLeaderboard(
            LeaderboardParams params, 
            Pageable pageable) {
        log.debug("Leaderboard request: type={}, period={}, minMatches={}, page={}", 
                params.getType(), params.getPeriod(), params.getMinMatches(), pageable.getPageNumber());
        
        var leaderboard = statisticsService.getLeaderboard(
                params.getType(), 
                params.getPeriod(), 
                params.getMinMatches(), 
                pageable);
        return ResponseEntity.ok(leaderboard);
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<PlayerStatsResponse> getPersonalStats(
            @RequestParam(required = false, defaultValue = "ALL_TIME") TimePeriod period) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Personal stats request: user={}, period={}", email, period);
        
        var stats = statisticsService.getPersonalStats(period);
        return ResponseEntity.ok(stats);
    }

    @Override
    @GetMapping("/h2h")
    public ResponseEntity<Page<H2HStatsResponse>> getH2HStats(
            H2HParams params, 
            Pageable pageable) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("H2H stats request: user={}, period={}, myPosition={}, opponentPosition={}, page={}", 
                email, params.getPeriod(), params.getMyPosition(), params.getOpponentPosition(), pageable.getPageNumber());
        
        var h2hStats = statisticsService.getH2HStats(
                params.getPeriod(), 
                params.getMyPosition(), 
                params.getOpponentPosition(), 
                pageable);
        return ResponseEntity.ok(h2hStats);
    }
}
