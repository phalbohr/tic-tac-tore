package com.tictactore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for submitting scores of a single game.
 */
public record GameRequest(
    @NotNull(message = "Team A score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @Max(value = 100, message = "Score cannot exceed 100")
    Integer teamAScore,

    @NotNull(message = "Team B score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @Max(value = 100, message = "Score cannot exceed 100")
    Integer teamBScore
) {
    /** Business limit: most foosball games end at 5-10, 100 is a safety ceiling. */
    public static final int MAX_SCORE = 100;
}
