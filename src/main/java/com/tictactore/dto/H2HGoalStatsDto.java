package com.tictactore.dto;

public record H2HGoalStatsDto(
        PositionalGoalMatrixDto attackerVsDefender,
        PositionalGoalMatrixDto attackerVsAttacker,
        PositionalGoalMatrixDto defenderVsAttacker,
        PositionalGoalMatrixDto defenderVsDefender
) {
    public static H2HGoalStatsDto empty() {
        return new H2HGoalStatsDto(
                PositionalGoalMatrixDto.empty(),
                PositionalGoalMatrixDto.empty(),
                PositionalGoalMatrixDto.empty(),
                PositionalGoalMatrixDto.empty()
        );
    }
}
