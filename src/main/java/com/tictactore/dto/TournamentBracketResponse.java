package com.tictactore.dto;

import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentStatus;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record TournamentBracketResponse(
        UUID tournamentId,
        String tournamentName,
        TournamentFormat format,
        TournamentMode mode,
        TournamentStatus status,
        int totalRounds,
        List<RoundMatchesResponse> rounds,
        List<TournamentRegistrationResponse> seededParticipants
) {}
