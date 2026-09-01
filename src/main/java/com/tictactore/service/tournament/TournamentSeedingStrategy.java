package com.tictactore.service.tournament;

import com.tictactore.dto.tournament.SeededParticipant;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentRegistration;

import java.util.List;

public interface TournamentSeedingStrategy {

    List<SeededParticipant> seed(Tournament tournament, List<TournamentRegistration> registrations);
}
