package com.tictactore.event;

import java.util.List;
import java.util.UUID;

public record TournamentMatchStartedEvent(
        UUID tournamentId,
        UUID matchId,
        List<UUID> participantUserIds
) {}
