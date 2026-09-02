package com.tictactore.service.tournament;

import com.tictactore.dto.tournament.TournamentStandingResponse;

import java.util.List;
import java.util.UUID;

public interface TournamentStandingsService {
    List<TournamentStandingResponse> calculateStandings(UUID tournamentId);
}
