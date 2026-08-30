package com.tictactore.dto;

import com.tictactore.model.MatchType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateChallengeRequest(
        UUID targetPlayerId,
        UUID targetGroupId,
        @NotNull(message = "Match type is required")
        MatchType matchType,
        UUID ruleConfigId,
        @Size(max = 255, message = "Message must not exceed 255 characters")
        String message
) {
}
