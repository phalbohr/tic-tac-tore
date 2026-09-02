package com.tictactore.event;

import java.util.List;
import java.util.UUID;

public record TournamentCancelledEvent(
        UUID tournamentId,
        String tournamentName,
        String reason,
        List<UUID> participantUserIds
) {}
