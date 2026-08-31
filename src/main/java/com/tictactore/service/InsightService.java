package com.tictactore.service;

import com.tictactore.dto.AchievementDto;
import com.tictactore.dto.PlayerInsightsResponse;
import com.tictactore.model.Match;
import com.tictactore.service.achievement.PlayerStatsContext;

import java.util.List;
import java.util.UUID;

public interface InsightService {

    PlayerInsightsResponse getPlayerInsights(UUID playerId);

    PlayerInsightsResponse generateInsights(
            UUID playerId,
            List<Match> matches,
            PlayerStatsContext stats,
            List<AchievementDto> achievements
    );
}
