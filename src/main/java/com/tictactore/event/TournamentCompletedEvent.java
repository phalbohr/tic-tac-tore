package com.tictactore.event;

import java.time.Instant;
import java.util.UUID;

public record TournamentCompletedEvent(
        UUID tournamentId,
        UUID winnerRegistrationId,
        Instant completedAt
) {}
