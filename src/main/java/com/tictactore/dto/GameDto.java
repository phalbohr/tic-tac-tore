package com.tictactore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GameDto(
    @Min(0) @Max(100) int teamAScore,
    @Min(0) @Max(100) int teamBScore
) {}
