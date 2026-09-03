package com.tictactore.dto.tournament;

import java.util.UUID;

public record TournamentStandingResponse(
        UUID registrationId,
        UUID userId,
        String nickname,
        String avatarUrl,
        UUID partnerUserId,
        String partnerNickname,
        String partnerAvatarUrl,
        int matchesPlayed,
        int wins,
        int losses,
        int gamesWon,
        int gamesLost,
        int gameDifference,
        int points,
        boolean isEliminated,
        Integer rank
) {}
