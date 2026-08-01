package com.tictactore.dto;

import java.time.Instant;
import java.util.UUID;

public record MatchConfirmationResponse(
    UUID matchId,
    String status,
    UUID confirmedByUserId,
    Instant confirmedAt,
    MatchResponse match
) {
    public MatchConfirmationResponse(MatchResponse match) {
        this(
            match.id(),
            match.status(),
            match.confirmedByUserId(),
            match.confirmedAt(),
            match
        );
    }
}
