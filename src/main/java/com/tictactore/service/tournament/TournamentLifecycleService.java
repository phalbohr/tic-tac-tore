package com.tictactore.service.tournament;

import com.tictactore.dto.TournamentResponse;
import com.tictactore.model.TournamentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TournamentLifecycleService {

    TournamentResponse startTournament(UUID tournamentId);

    Page<TournamentResponse> getTournaments(TournamentStatus status, Pageable pageable);
}
