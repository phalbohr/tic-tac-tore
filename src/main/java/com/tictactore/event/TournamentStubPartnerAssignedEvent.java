package com.tictactore.event;

import java.util.UUID;

public record TournamentStubPartnerAssignedEvent(
        UUID tournamentId,
        UUID matchId,
        UUID deletedUserId,
        UUID teammateUserId,
        UUID stubPartnerUserId
) {}
