package com.tictactore.event;

import com.tictactore.model.MatchType;

import java.util.UUID;

public record ChallengeCreatedEvent(
        UUID challengeId,
        UUID challengerId,
        String challengerNickname,
        UUID targetPlayerId,
        UUID targetGroupId,
        MatchType matchType
) {
}
