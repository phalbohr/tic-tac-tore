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
public class FormTrendInsightGenerator implements InsightGenerator {

    @Override
    public Optional<PlayerInsightDto> generate(
            UUID playerId,
            List<Match> matches,
            PlayerStatsContext stats,
            List<AchievementDto> achievements
    ) {
        if (stats == null || stats.totalMatches() < 10 || matches == null || matches.size() < 5) {
            return Optional.empty();
        }

        List<Match> recentMatches = matches.subList(0, Math.min(10, matches.size()));
        int recentWins = 0;
        for (Match match : recentMatches) {
            if (InsightMatchUtils.isPlayerWinner(match, playerId)) {
                recentWins++;
            }
        }

        double recentWinRate = ((double) recentWins / recentMatches.size()) * 100.0;
        double careerWinRate = ((double) stats.totalWins() / stats.totalMatches()) * 100.0;
        double diff = recentWinRate - careerWinRate;

        if (diff >= 15.0) {
            return Optional.of(new PlayerInsightDto(
                    UUID.randomUUID(),
                    InsightType.FORM_TREND,
                    InsightCategory.TREND,
                    InsightImportance.HIGH,
                    "insights.formTrend.title",
                    "insights.formTrend.description",
                    Map.of(
                            "recentWinRate", Math.round(recentWinRate),
                            "careerWinRate", Math.round(careerWinRate),
                            "diff", Math.round(diff)
                    ),
                    "trending_up",
                    null
            ));
        }

        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
