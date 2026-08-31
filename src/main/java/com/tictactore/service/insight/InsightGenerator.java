package com.tictactore.service.insight;

import com.tictactore.dto.AchievementDto;
import com.tictactore.dto.PlayerInsightDto;
import com.tictactore.model.Match;
import com.tictactore.service.achievement.PlayerStatsContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InsightGenerator {

    Optional<PlayerInsightDto> generate(
            UUID playerId,
            List<Match> matches,
            PlayerStatsContext stats,
            List<AchievementDto> achievements
    );

    int getOrder();
}
