package com.tictactore.dto;

import java.util.UUID;

public record LeaderboardEntry(
    UUID playerId,
    String playerName,
    int totalMatches,
    int wins,
    int losses,
    double winRate
) {
}
