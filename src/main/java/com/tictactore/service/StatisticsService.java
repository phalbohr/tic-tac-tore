package com.tictactore.service;

import com.tictactore.dto.PagedResponse;
import com.tictactore.dto.TeamPairStatsResponse;
import com.tictactore.dto.TimePeriod;

import java.util.UUID;

public interface StatisticsService {
    PagedResponse<TeamPairStatsResponse> getTeamPairStats(
            UUID playerId,
            TimePeriod period,
            UUID ruleConfigId,
            int page,
            int size,
            int minMatches
    );
}
