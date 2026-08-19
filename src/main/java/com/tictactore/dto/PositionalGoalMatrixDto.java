package com.tictactore.dto;

public record PositionalGoalMatrixDto(
        long scored,
        long conceded
) {
    public static PositionalGoalMatrixDto empty() {
        return new PositionalGoalMatrixDto(0, 0);
    }
}
