package com.tictactore.dto;

import com.tictactore.model.MatchType;
import com.tictactore.model.PoolStatus;
import com.tictactore.model.SkillLevel;
import com.tictactore.model.StartCondition;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PoolResponse(
        UUID id,
        UUID creatorId,
        String creatorNickname,
        MatchType matchType,
        StartCondition startCondition,
        Instant scheduledTime,
        SkillLevel skillLevel,
        PoolStatus status,
        int requiredPlayers,
        int currentPlayers,
        List<PoolParticipantDto> participants,
        Instant createdAt
) {}
