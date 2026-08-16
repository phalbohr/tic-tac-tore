package com.tictactore.controller;

import com.tictactore.dto.PagedResponse;
import com.tictactore.dto.TeamPairStatsResponse;
import com.tictactore.dto.TimePeriod;
import com.tictactore.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

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
}
