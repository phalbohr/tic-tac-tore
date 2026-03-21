package com.tictactore.dto.statistics;

import lombok.Builder;

/**
 * Record for position-specific or overall match statistics.
 */
@Builder
public record PositionStatsResponse(
    Long matches,
    Long wins,
    Long losses,
    Double winRate
) {}
