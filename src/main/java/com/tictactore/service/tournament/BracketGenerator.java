package com.tictactore.service.tournament;

import com.tictactore.dto.tournament.SeededParticipant;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMatch;

import java.util.List;

public interface BracketGenerator {

    List<TournamentMatch> generateBracket(Tournament tournament, List<SeededParticipant> seededParticipants);
}
