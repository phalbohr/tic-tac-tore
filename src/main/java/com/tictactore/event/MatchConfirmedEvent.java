package com.tictactore.event;

import java.util.List;
import java.util.UUID;

public record MatchConfirmedEvent(
        UUID matchId,
        List<UUID> participantIds
) {
}
