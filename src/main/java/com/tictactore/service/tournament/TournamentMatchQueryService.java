package com.tictactore.service.tournament;

import com.tictactore.dto.TournamentBracketResponse;
import com.tictactore.dto.TournamentMatchResponse;

import java.util.List;
import java.util.UUID;

public interface TournamentMatchQueryService {

    TournamentBracketResponse getTournamentBracket(UUID tournamentId);

    List<TournamentMatchResponse> getTournamentMatches(UUID tournamentId, Integer round);
}
