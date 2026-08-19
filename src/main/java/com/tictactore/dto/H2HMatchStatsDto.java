package com.tictactore.dto;

public record H2HMatchStatsDto(
        long matches,
        long wins,
        long losses,
        long draws,
        double winRate
) {
    public static H2HMatchStatsDto empty() {
        return new H2HMatchStatsDto(0, 0, 0, 0, 0.0);
    }
}
