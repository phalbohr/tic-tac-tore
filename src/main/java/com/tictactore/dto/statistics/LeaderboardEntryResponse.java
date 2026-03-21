package com.tictactore.dto.statistics;

import lombok.Builder;
import java.util.UUID;

/**
 * Record for a single entry in the leaderboard.
 */
@Builder
public record LeaderboardEntryResponse(
    Integer rank,
    UUID playerId,
    String playerName,
    Long totalMatches,
    Long wins,
    Long losses,
    Double winRate
) {}
