package com.tictactore.event;

import com.tictactore.model.MatchType;

import java.util.UUID;

public record ChallengeAcceptedEvent(
        UUID challengeId,
        UUID challengerId,
        UUID targetUserId,
        String targetNickname,
        MatchType matchType
) {
}
