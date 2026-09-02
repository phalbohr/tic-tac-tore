package com.tictactore.dto.tournament;

import java.util.UUID;

public record TournamentStandingResponse(
        UUID registrationId,
        UUID userId,
        String nickname,
        int matchesPlayed,
        int wins,
        int losses,
        int points,
        boolean isEliminated
) {}
