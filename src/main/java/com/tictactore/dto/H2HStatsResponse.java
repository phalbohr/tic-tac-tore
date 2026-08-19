package com.tictactore.dto;

public record H2HStatsResponse(
        PlayerSummaryDto opponent,
        H2HMatchTableDto matches,
        H2HGameTableDto games,
        H2HGoalStatsDto goals
) {}
