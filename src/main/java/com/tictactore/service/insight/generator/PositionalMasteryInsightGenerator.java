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
public class PositionalMasteryInsightGenerator implements InsightGenerator {

    @Override
    public Optional<PlayerInsightDto> generate(
            UUID playerId,
            List<Match> matches,
            PlayerStatsContext stats,
            List<AchievementDto> achievements
    ) {
        if (matches == null || matches.isEmpty()) {
            return Optional.empty();
        }

        int attMatches = 0;
        int attWins = 0;
        int defMatches = 0;
        int defWins = 0;

        for (Match match : matches) {
            boolean isAtt = playerId.equals(match.getTeamAAttackerId()) || playerId.equals(match.getTeamBAttackerId());
            boolean isDef = playerId.equals(match.getTeamADefenderId()) || playerId.equals(match.getTeamBDefenderId());
            boolean won = InsightMatchUtils.isPlayerWinner(match, playerId);

            if (isAtt) {
                attMatches++;
                if (won) {
                    attWins++;
                }
            } else if (isDef) {
                defMatches++;
                if (won) {
                    defWins++;
                }
            }
        }

        if (attMatches < 5 || defMatches < 5) {
            return Optional.empty();
        }

        double attWinRate = ((double) attWins / attMatches) * 100.0;
        double defWinRate = ((double) defWins / defMatches) * 100.0;
        double delta = Math.abs(attWinRate - defWinRate);

        if (delta >= 20.0) {
            String favored = attWinRate > defWinRate ? "Attacker" : "Defender";
            long higher = Math.round(Math.max(attWinRate, defWinRate));
            long lower = Math.round(Math.min(attWinRate, defWinRate));

            return Optional.of(new PlayerInsightDto(
                    UUID.randomUUID(),
                    InsightType.POSITIONAL_MASTERY,
                    InsightCategory.POSITION,
                    InsightImportance.MEDIUM,
                    "insights.positionalMastery.title",
                    "insights.positionalMastery.description",
                    Map.of(
                            "favoredPosition", favored,
                            "higherWinRate", higher,
                            "lowerWinRate", lower
                    ),
                    "sports_score",
                    null
            ));
        }

        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return 30;
    }
}
