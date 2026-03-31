package com.tictactore.dto.statistics;

import lombok.Builder;
import java.util.UUID;

/**
 * Record for player's detailed statistics profile.
 */
@Builder
public record PlayerStatsResponse(
    UUID playerId,
    String playerName,
    PositionStatsResponse overall,
    PositionStatsResponse attacker,
    PositionStatsResponse defender
) {}
