package com.tictactore.dto;

import lombok.Builder;

/**
 * Record for returning game details in API responses.
 */
@Builder
public record GameResponse(
    Integer gameNumber,
    Integer teamAScore,
    Integer teamBScore
) {}
