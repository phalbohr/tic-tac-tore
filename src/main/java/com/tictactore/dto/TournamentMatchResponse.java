package com.tictactore.dto;

import com.tictactore.model.TournamentMatchStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record TournamentMatchResponse(
        UUID id,
        UUID tournamentId,
        int round,
        int matchOrder,
        UUID matchId,
        TournamentRegistrationResponse participant1,
        TournamentRegistrationResponse participant2,
        Integer seed1,
        Integer seed2,
        TournamentMatchStatus status,
        UUID winnerRegistrationId,
        UUID nextMatchId,
        Instant createdAt
) {}
