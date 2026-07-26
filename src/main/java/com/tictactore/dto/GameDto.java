package com.tictactore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record GameDto(
    @Min(0) @Max(100) int teamAScore,
    @Min(0) @Max(100) int teamBScore,
    UUID teamAAttackerId,
    UUID teamADefenderId,
    UUID teamBAttackerId,
    UUID teamBDefenderId
) {
    public GameDto(int teamAScore, int teamBScore) {
        this(teamAScore, teamBScore, null, null, null, null);
    }
}
