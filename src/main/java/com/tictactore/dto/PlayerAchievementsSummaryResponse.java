package com.tictactore.dto;

import java.util.List;
import java.util.UUID;

public record PlayerAchievementsSummaryResponse(
        UUID playerId,
        int totalUnlocked,
        int totalAvailable,
        List<AchievementDto> achievements
) {
}
