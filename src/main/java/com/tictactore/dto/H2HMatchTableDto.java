package com.tictactore.dto;

public record H2HMatchTableDto(
        H2HMatchStatsDto with,
        H2HMatchStatsDto vs
) {
    public static H2HMatchTableDto empty() {
        return new H2HMatchTableDto(H2HMatchStatsDto.empty(), H2HMatchStatsDto.empty());
    }
}
