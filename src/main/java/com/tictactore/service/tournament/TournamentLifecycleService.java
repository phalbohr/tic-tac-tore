package com.tictactore.service.tournament;

import com.tictactore.dto.TournamentResponse;

import java.util.UUID;

public interface TournamentLifecycleService {

    TournamentResponse startTournament(UUID tournamentId);
}
