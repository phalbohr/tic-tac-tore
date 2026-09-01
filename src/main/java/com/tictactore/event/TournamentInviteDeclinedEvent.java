package com.tictactore.event;

import java.util.UUID;

public record TournamentInviteDeclinedEvent(
        UUID registrationId,
        UUID tournamentId,
        String tournamentName,
        UUID partnerId,
        String partnerNickname,
        UUID inviterId
) {}
