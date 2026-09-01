package com.tictactore.event;

import java.util.UUID;

public record TournamentInviteCreatedEvent(
        UUID registrationId,
        UUID tournamentId,
        String tournamentName,
        UUID inviterId,
        String inviterNickname,
        UUID partnerId
) {}
