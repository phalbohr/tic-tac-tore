package com.tictactore.event;

import com.tictactore.model.MatchType;
import com.tictactore.model.SkillLevel;

import java.util.UUID;

public record PoolCreatedEvent(
    UUID poolId,
    UUID creatorId,
    MatchType matchType,
    SkillLevel skillLevel,
    String creatorNickname
) {}
