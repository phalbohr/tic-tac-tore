package com.tictactore.service.tournament;

import com.tictactore.dto.TournamentMatchResponse;

import java.util.UUID;

public interface TournamentMatchService {

    TournamentMatchResponse startMatch(UUID tournamentId, UUID tournamentMatchId, UUID currentUserId);

    TournamentMatchResponse cancelMatch(UUID tournamentId, UUID tournamentMatchId, UUID currentUserId);

    void completeMatch(UUID tournamentMatchId, UUID matchId);
}
