package com.tictactore.dto;

public record H2HGameTableDto(
        H2HGameStatsDto with,
        H2HGameStatsDto vs
) {
    public static H2HGameTableDto empty() {
        return new H2HGameTableDto(H2HGameStatsDto.empty(), H2HGameStatsDto.empty());
    }
}
