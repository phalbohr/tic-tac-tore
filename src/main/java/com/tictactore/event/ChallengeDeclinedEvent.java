package com.tictactore.event;

import java.util.UUID;

public record ChallengeDeclinedEvent(
        UUID challengeId,
        UUID challengerId,
        UUID targetUserId,
        String targetNickname
) {
}
