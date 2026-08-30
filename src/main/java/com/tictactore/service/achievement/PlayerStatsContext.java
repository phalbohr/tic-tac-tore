package com.tictactore.service.achievement;

import java.util.UUID;

public record PlayerStatsContext(
        UUID userId,
        long totalMatches,
        long totalWins,
        long totalGoalsAsAttacker,
        long totalMatchesAsDefender
) {
}
