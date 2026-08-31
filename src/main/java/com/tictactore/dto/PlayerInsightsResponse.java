package com.tictactore.dto;

import java.util.List;
import java.util.UUID;

public record PlayerInsightsResponse(
        UUID playerId,
        int totalCount,
        List<PlayerInsightDto> insights
) {
}
