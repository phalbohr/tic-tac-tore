package com.tictactore.dto;

import com.tictactore.model.PoolParticipantRole;

import java.time.Instant;
import java.util.UUID;

public record PoolParticipantDto(
        UUID userId,
        String nickname,
        String avatar,
        PoolParticipantRole role,
        Instant joinedAt
) {}
