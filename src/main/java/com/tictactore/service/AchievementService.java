package com.tictactore.service;

import com.tictactore.dto.PlayerAchievementsSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface AchievementService {

    PlayerAchievementsSummaryResponse getPlayerAchievements(UUID playerId);

    void evaluateMatchAchievements(UUID matchId, List<UUID> participantIds);
}
