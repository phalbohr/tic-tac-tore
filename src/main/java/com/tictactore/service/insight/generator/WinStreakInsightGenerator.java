package com.tictactore.service.insight.generator;

import com.tictactore.dto.AchievementDto;
import com.tictactore.dto.InsightCategory;
import com.tictactore.dto.InsightImportance;
import com.tictactore.dto.InsightType;
import com.tictactore.dto.PlayerInsightDto;
import com.tictactore.model.Match;
import com.tictactore.service.achievement.PlayerStatsContext;
import com.tictactore.service.insight.InsightGenerator;
import com.tictactore.service.insight.InsightMatchUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class WinStreakInsightGenerator implements InsightGenerator {

    @Override
    public Optional<PlayerInsightDto> generate(
            UUID playerId,
            List<Match> matches,
            PlayerStatsContext stats,
            List<AchievementDto> achievements
    ) {
        if (matches == null || matches.size() < 3) {
            return Optional.empty();
        }

        int streak = 0;
        for (Match match : matches) {
            if (InsightMatchUtils.isPlayerWinner(match, playerId)) {
                streak++;
            } else {
                break;
            }
        }

        if (streak >= 3) {
            return Optional.of(new PlayerInsightDto(
                    UUID.randomUUID(),
                    InsightType.WIN_STREAK,
                    InsightCategory.STREAK,
                    InsightImportance.HIGH,
                    "insights.winStreak.title",
                    "insights.winStreak.description",
                    Map.of("streak", streak),
                    "local_fire_department",
                    null
            ));
        }

        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
