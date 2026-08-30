package com.tictactore.service.achievement;

import com.tictactore.model.Match;

import java.util.UUID;

public interface AchievementEvaluator {
    String getAchievementCode();

    boolean evaluate(UUID userId, Match match, PlayerStatsContext stats);
}
