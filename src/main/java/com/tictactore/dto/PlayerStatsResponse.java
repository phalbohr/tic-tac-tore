package com.tictactore.dto;

import java.util.UUID;

public record PlayerStatsResponse(
    UUID playerId,
    String playerName,
    PositionStatsResponse overall,
    PositionStatsResponse attacker,
    PositionStatsResponse defender
) {
    public record PositionStatsResponse(
        int matches,
        int wins,
        int losses,
        double winRate
    ) {
        public static PositionStatsResponse empty() {
            return new PositionStatsResponse(0, 0, 0, 0.0);
        }
    }
}
