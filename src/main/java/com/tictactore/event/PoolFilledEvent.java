package com.tictactore.event;

import com.tictactore.model.MatchType;

import java.util.List;
import java.util.UUID;

public record PoolFilledEvent(
    UUID poolId,
    MatchType matchType,
    List<UUID> participantUserIds
) {}
