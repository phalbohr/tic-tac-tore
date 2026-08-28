package com.tictactore.dto;

import com.tictactore.model.MatchType;
import com.tictactore.model.SkillLevel;
import com.tictactore.model.StartCondition;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreatePoolRequest(
        @NotNull(message = "Match type is required")
        MatchType matchType,

        @NotNull(message = "Start condition is required")
        StartCondition startCondition,

        Instant scheduledTime,

        SkillLevel skillLevel
) {
    public CreatePoolRequest {
        if (skillLevel == null) {
            skillLevel = SkillLevel.OPEN_FOR_ALL;
        }
    }
}
