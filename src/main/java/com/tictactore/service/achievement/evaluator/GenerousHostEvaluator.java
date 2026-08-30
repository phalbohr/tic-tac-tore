package com.tictactore.service.achievement.evaluator;

import com.tictactore.model.Match;
import com.tictactore.service.achievement.AchievementEvaluator;
import com.tictactore.service.achievement.PlayerStatsContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GenerousHostEvaluator implements AchievementEvaluator {

    public static final String CODE = "GENEROUS_HOST";
    public static final int CONCEDED_THRESHOLD = 10;

    @Override
    public String getAchievementCode() {
        return CODE;
    }

    @Override
    public boolean evaluate(UUID userId, Match match, PlayerStatsContext stats) {
        if (userId == null || match == null || match.getGames() == null || match.getGames().isEmpty()) {
            return false;
        }
        boolean onTeamA = userId.equals(match.getTeamAAttackerId()) || userId.equals(match.getTeamADefenderId());
        boolean onTeamB = userId.equals(match.getTeamBAttackerId()) || userId.equals(match.getTeamBDefenderId());
        if (!onTeamA && !onTeamB) {
            return false;
        }

        if (onTeamA) {
            return match.getGames().stream().anyMatch(g -> g.getTeamBScore() >= CONCEDED_THRESHOLD);
        } else {
            return match.getGames().stream().anyMatch(g -> g.getTeamAScore() >= CONCEDED_THRESHOLD);
        }
    }
}
