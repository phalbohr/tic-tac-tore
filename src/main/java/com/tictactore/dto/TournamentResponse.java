package com.tictactore.dto;

import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record TournamentResponse(
        UUID id,
        String name,
        TournamentFormat format,
        TournamentMode mode,
        UUID ruleConfigurationId,
        int minParticipants,
        int maxParticipants,
        Instant registrationDeadline,
        Integer roundCount,
        boolean hasPlayoff,
        TournamentStatus status,
        UUID creatorId,
        Instant createdAt,
        Instant updatedAt
) {}
