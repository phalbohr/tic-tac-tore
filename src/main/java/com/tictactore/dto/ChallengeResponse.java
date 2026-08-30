package com.tictactore.dto;

import com.tictactore.model.ChallengeStatus;
import com.tictactore.model.MatchType;

import java.time.Instant;
import java.util.UUID;

public record ChallengeResponse(
        UUID id,
        UUID challengerId,
        String challengerNickname,
        String challengerAvatar,
        UUID targetPlayerId,
        String targetPlayerNickname,
        String targetPlayerAvatar,
        UUID targetGroupId,
        String targetGroupName,
        MatchType matchType,
        UUID ruleConfigId,
        String ruleConfigName,
        String message,
        ChallengeStatus status,
        Instant createdAt,
        Instant expiresAt
) {
}
