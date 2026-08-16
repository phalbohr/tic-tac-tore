package com.tictactore.dto;

import java.util.UUID;

public record TeamPairStatsResponse(
        UUID attackerId,
        String attackerName,
        String attackerAvatar,
        UUID defenderId,
        String defenderName,
        String defenderAvatar,
        long matches,
        long wins,
        long losses,
        double winRate
) {}
