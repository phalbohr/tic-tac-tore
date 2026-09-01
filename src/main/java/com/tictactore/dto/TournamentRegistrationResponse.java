package com.tictactore.dto;

import com.tictactore.model.RegistrationStatus;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record TournamentRegistrationResponse(
        UUID id,
        UUID tournamentId,
        String tournamentName,
        UUID playerId,
        String playerNickname,
        String playerAvatarUrl,
        UUID partnerId,
        String partnerNickname,
        String partnerAvatarUrl,
        RegistrationStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
