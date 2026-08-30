package com.tictactore.service.achievement.evaluator;

import com.tictactore.model.Match;
import com.tictactore.service.achievement.AchievementEvaluator;
import com.tictactore.service.achievement.PlayerStatsContext;
import com.tictactore.service.achievement.ProgressInfo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FirstWinEvaluator implements AchievementEvaluator {

    public static final String CODE = "FIRST_WIN";

    @Override
    public String getAchievementCode() {
        return CODE;
    }

    @Override
    public boolean evaluate(UUID userId, Match match, PlayerStatsContext stats) {
        if (userId == null) {
            return false;
        }
        if (stats != null && stats.totalWins() >= 1) {
            return true;
        }
        return isPlayerWinnerInMatch(userId, match);
    }

    @Override
    public ProgressInfo getProgress(UUID userId, PlayerStatsContext stats) {
        if (stats == null) {
            return new ProgressInfo(0, 1, true);
        }
        return new ProgressInfo(Math.min(stats.totalWins(), 1), 1, true);
    }

    private boolean isPlayerWinnerInMatch(UUID userId, Match match) {
        if (match == null || match.getGames() == null || match.getGames().isEmpty()) {
            return false;
        }
        boolean onTeamA = userId.equals(match.getTeamAAttackerId()) || userId.equals(match.getTeamADefenderId());
        boolean onTeamB = userId.equals(match.getTeamBAttackerId()) || userId.equals(match.getTeamBDefenderId());
        if (!onTeamA && !onTeamB) {
            return false;
        }
        long teamAWins = match.getGames().stream().filter(g -> g.getTeamAScore() > g.getTeamBScore()).count();
        long teamBWins = match.getGames().stream().filter(g -> g.getTeamBScore() > g.getTeamAScore()).count();
        return (onTeamA && teamAWins > teamBWins) || (onTeamB && teamBWins > teamAWins);
    }
}
