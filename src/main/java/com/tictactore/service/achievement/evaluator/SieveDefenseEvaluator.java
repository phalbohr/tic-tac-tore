package com.tictactore.service.achievement.evaluator;

import com.tictactore.model.Match;
import com.tictactore.service.achievement.AchievementEvaluator;
import com.tictactore.service.achievement.PlayerStatsContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SieveDefenseEvaluator implements AchievementEvaluator {

    public static final String CODE = "SIEVE_DEFENSE";
    public static final int CONCEDED_GOALS_THRESHOLD = 15;

    @Override
    public String getAchievementCode() {
        return CODE;
    }

    @Override
    public boolean evaluate(UUID userId, Match match, PlayerStatsContext stats) {
        if (userId == null || match == null || match.getGames() == null || match.getGames().isEmpty()) {
            return false;
        }

        boolean isDefenderTeamA = userId.equals(match.getTeamADefenderId());
        boolean isDefenderTeamB = userId.equals(match.getTeamBDefenderId());
        if (!isDefenderTeamA && !isDefenderTeamB) {
            return false;
        }

        int totalConcededGoals = isDefenderTeamA
                ? match.getGames().stream().mapToInt(g -> g.getTeamBScore()).sum()
                : match.getGames().stream().mapToInt(g -> g.getTeamAScore()).sum();

        return totalConcededGoals >= CONCEDED_GOALS_THRESHOLD;
    }
}
