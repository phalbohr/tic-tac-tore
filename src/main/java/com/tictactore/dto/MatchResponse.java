package com.tictactore.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MatchResponse(
    UUID id,
    String idempotencyKey,
    UUID creatorId,
    UUID teamAAttackerId,
    UUID teamADefenderId,
    UUID teamBAttackerId,
    UUID teamBDefenderId,
    String status,
    List<GameDto> games,
    Instant createdAt,
    UUID confirmedByUserId,
    Instant confirmedAt
) {
    public MatchResponse(
        UUID id,
        String idempotencyKey,
        UUID creatorId,
        UUID teamAAttackerId,
        UUID teamADefenderId,
        UUID teamBAttackerId,
        UUID teamBDefenderId,
        String status,
        List<GameDto> games,
        Instant createdAt
    ) {
        this(id, idempotencyKey, creatorId, teamAAttackerId, teamADefenderId, teamBAttackerId, teamBDefenderId, status, games, createdAt, null, null);
    }
}
