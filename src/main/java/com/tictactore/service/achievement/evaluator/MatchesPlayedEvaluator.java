package com.tictactore.service.achievement.evaluator;

import com.tictactore.model.Match;
import com.tictactore.service.achievement.AchievementEvaluator;
import com.tictactore.service.achievement.PlayerStatsContext;
import com.tictactore.service.achievement.ProgressInfo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MatchesPlayedEvaluator implements AchievementEvaluator {

    public static final String CODE = "MATCHES_10";
    public static final int THRESHOLD = 10;

    @Override
    public String getAchievementCode() {
        return CODE;
    }

    @Override
    public boolean evaluate(UUID userId, Match match, PlayerStatsContext stats) {
        if (userId == null || stats == null) {
            return false;
        }
        return stats.totalMatches() >= THRESHOLD;
    }

    @Override
    public ProgressInfo getProgress(UUID userId, PlayerStatsContext stats) {
        if (stats == null) {
            return new ProgressInfo(0, THRESHOLD, true);
        }
        return new ProgressInfo(Math.min(stats.totalMatches(), THRESHOLD), THRESHOLD, true);
    }
}
