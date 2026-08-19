package com.tictactore.dto;

public record H2HGameStatsDto(
        long gamesWon,
        long gamesLost,
        long totalGames,
        double winRate
) {
    public static H2HGameStatsDto empty() {
        return new H2HGameStatsDto(0, 0, 0, 0.0);
    }
}
