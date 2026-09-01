package com.tictactore.event;

import java.util.UUID;

public record TournamentRegistrationCancelledEvent(
        UUID registrationId,
        UUID tournamentId,
        String tournamentName,
        UUID cancelledById,
        String cancelledByName,
        UUID notifyUserId
) {
}
