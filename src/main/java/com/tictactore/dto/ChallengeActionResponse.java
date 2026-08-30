package com.tictactore.dto;

import com.tictactore.model.ChallengeStatus;

import java.util.UUID;

public record ChallengeActionResponse(
        UUID challengeId,
        ChallengeStatus status,
        String message
) {
}
