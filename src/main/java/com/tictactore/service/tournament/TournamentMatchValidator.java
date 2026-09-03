package com.tictactore.service.tournament;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.model.TournamentMatch;

public interface TournamentMatchValidator {

    void validateTournamentMatchCreation(TournamentMatch tournamentMatch, CreateMatchRequest request);
}
