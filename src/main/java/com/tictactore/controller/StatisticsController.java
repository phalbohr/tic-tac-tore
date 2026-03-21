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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController implements StatisticsApi {

    private final StatisticsService statisticsService;

    @Override
    public ResponseEntity<Page<LeaderboardEntryResponse>> getLeaderboard(
            LeaderboardType type, 
            TimePeriod period, 
            Integer minMatches, 
            Pageable pageable) {
        log.debug("Leaderboard request: type={}, period={}, minMatches={}, page={}", 
                type, period, minMatches, pageable.getPageNumber());
        
        var leaderboard = statisticsService.getLeaderboard(type, period, minMatches, pageable);
        return ResponseEntity.ok(leaderboard);
    }

    @Override
    public ResponseEntity<PlayerStatsResponse> getPersonalStats(TimePeriod period) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Personal stats request: user={}, period={}", email, period);
        
        var stats = statisticsService.getPersonalStats(period);
        return ResponseEntity.ok(stats);
    }

    @Override
    public ResponseEntity<Page<H2HStatsResponse>> getH2HStats(TimePeriod period, Pageable pageable) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("H2H stats request: user={}, period={}, page={}", email, period, pageable.getPageNumber());
        
        var h2hStats = statisticsService.getH2HStats(period, pageable);
        return ResponseEntity.ok(h2hStats);
    }
}
