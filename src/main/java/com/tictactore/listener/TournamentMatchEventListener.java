package com.tictactore.listener;

import com.tictactore.event.MatchConfirmedEvent;
import com.tictactore.model.TournamentMatch;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.service.tournament.TournamentMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TournamentMatchEventListener {

    private final TournamentMatchRepository tournamentMatchRepository;
    private final TournamentMatchService tournamentMatchService;

    @EventListener
    @Transactional
    public void handleMatchConfirmed(MatchConfirmedEvent event) {
        if (event == null || event.matchId() == null) {
            return;
        }

        Optional<TournamentMatch> tournamentMatchOpt = tournamentMatchRepository.findByMatchId(event.matchId());
        if (tournamentMatchOpt.isEmpty()) {
            return;
        }

        tournamentMatchService.completeMatch(tournamentMatchOpt.get().getId(), event.matchId());
    }
}
