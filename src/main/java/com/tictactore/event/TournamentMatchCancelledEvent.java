package com.tictactore.event;

import java.util.UUID;

public record TournamentMatchCancelledEvent(
        UUID tournamentId,
        UUID matchId,
        UUID cancelledByUserId
) {}
