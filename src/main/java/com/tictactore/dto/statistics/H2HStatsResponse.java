package com.tictactore.dto.statistics;

import lombok.Builder;
import java.util.UUID;

/**
 * Record for Head-to-Head statistics against a specific opponent.
 */
@Builder
public record H2HStatsResponse(
    UUID opponentId,
    String opponentName,
    Long matches,
    Long wins,
    Long losses,
    Double winRate
) {}
