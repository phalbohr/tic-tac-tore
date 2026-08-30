package com.tictactore.service.achievement.evaluator;

import com.tictactore.model.Match;
import com.tictactore.service.achievement.AchievementEvaluator;
import com.tictactore.service.achievement.PlayerStatsContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HeartbreakerEvaluator implements AchievementEvaluator {

    public static final String CODE = "HEARTBREAKER";

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

        long teamAWins = match.getGames().stream().filter(g -> g.getTeamAScore() > g.getTeamBScore()).count();
        long teamBWins = match.getGames().stream().filter(g -> g.getTeamBScore() > g.getTeamAScore()).count();

        boolean playerTeamLost = (onTeamA && teamBWins > teamAWins) || (onTeamB && teamAWins > teamBWins);
        if (!playerTeamLost) {
            return false;
        }

        var decidingGame = match.getGames().get(match.getGames().size() - 1);
        int goalDifference = onTeamA
                ? decidingGame.getTeamBScore() - decidingGame.getTeamAScore()
                : decidingGame.getTeamAScore() - decidingGame.getTeamBScore();

        return goalDifference == 1;
    }
}
