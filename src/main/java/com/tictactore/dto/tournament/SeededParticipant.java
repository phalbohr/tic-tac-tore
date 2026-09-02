package com.tictactore.dto.tournament;

import com.tictactore.model.TournamentRegistration;

public record SeededParticipant(
        TournamentRegistration registration,
        int seed,
        double strengthScore
) {}
