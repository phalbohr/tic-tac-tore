package com.tictactore.event;

import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMode;

import java.util.List;
import java.util.UUID;

public record TournamentStartedEvent(
        UUID tournamentId,
        String tournamentName,
        TournamentFormat format,
        TournamentMode mode,
        List<UUID> participantUserIds,
        int totalMatches
) {}
