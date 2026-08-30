package com.tictactore.service.achievement.evaluator;

import com.tictactore.model.Match;
import com.tictactore.service.achievement.AchievementEvaluator;
import com.tictactore.service.achievement.PlayerStatsContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DefenseWallEvaluator implements AchievementEvaluator {

    public static final String CODE = "DEFENSE_WALL";
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
        return stats.totalMatchesAsDefender() >= THRESHOLD;
    }
}
