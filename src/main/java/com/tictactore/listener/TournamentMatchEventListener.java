package com.tictactore.listener;

import com.tictactore.event.MatchConfirmedEvent;
import com.tictactore.model.Match;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.service.tournament.TournamentStandingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TournamentMatchEventListener {

    private final TournamentMatchRepository tournamentMatchRepository;
    private final MatchRepository matchRepository;
    private final TournamentStandingsService tournamentStandingsService;

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

        TournamentMatch tournamentMatch = tournamentMatchOpt.get();
        Match match = matchRepository.findById(event.matchId()).orElse(null);
        if (match != null) {
            tournamentMatch.setMatch(match);
        }

        tournamentMatch.setStatus(TournamentMatchStatus.COMPLETED);
        TournamentRegistration winner = determineWinner(tournamentMatch);
        tournamentMatch.setWinner(winner);

        if (tournamentMatch.getTournament().getFormat() == TournamentFormat.CUP
                && tournamentMatch.getNextMatch() != null
                && winner != null) {
            advanceWinnerInCup(tournamentMatch, winner);
        }

        tournamentMatchRepository.save(tournamentMatch);
        tournamentStandingsService.calculateStandings(tournamentMatch.getTournament().getId());
    }

    private TournamentRegistration determineWinner(TournamentMatch tournamentMatch) {
        if (tournamentMatch.getMatch() == null || tournamentMatch.getMatch().getGames() == null) {
            return tournamentMatch.getParticipant1();
        }

        int teamAWins = 0;
        int teamBWins = 0;
        for (var game : tournamentMatch.getMatch().getGames()) {
            if (game.getTeamAScore() > game.getTeamBScore()) {
                teamAWins++;
            } else if (game.getTeamBScore() > game.getTeamAScore()) {
                teamBWins++;
            }
        }
        return teamAWins >= teamBWins ? tournamentMatch.getParticipant1() : tournamentMatch.getParticipant2();
    }

    private void advanceWinnerInCup(TournamentMatch tournamentMatch, TournamentRegistration winner) {
        TournamentMatch nextMatch = tournamentMatch.getNextMatch();
        if (tournamentMatch.getMatchOrder() % 2 == 1) {
            nextMatch.setParticipant1(winner);
            nextMatch.setSeed1(tournamentMatch.getSeed1());
        } else {
            nextMatch.setParticipant2(winner);
            nextMatch.setSeed2(tournamentMatch.getSeed2());
        }

        if (nextMatch.getParticipant1() != null && nextMatch.getParticipant2() != null
                && nextMatch.getStatus() == TournamentMatchStatus.PENDING) {
            nextMatch.setStatus(TournamentMatchStatus.READY);
        }
        tournamentMatchRepository.save(nextMatch);
    }
}
