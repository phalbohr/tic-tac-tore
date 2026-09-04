package com.tictactore.service.tournament.impl;

import com.tictactore.model.Match;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentStatus;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.service.operation.MatchOperation;
import com.tictactore.service.tournament.TournamentConfirmationDeadlineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class TournamentConfirmationDeadlineServiceImpl implements TournamentConfirmationDeadlineService {

    private final TournamentMatchRepository tournamentMatchRepository;
    private final MatchOperation matchOperation;
    private final int deadlineHours;

    public TournamentConfirmationDeadlineServiceImpl(
            TournamentMatchRepository tournamentMatchRepository,
            MatchOperation matchOperation,
            @Value("${app.tournament.confirmation-deadline-hours:48}") int deadlineHours
    ) {
        this.tournamentMatchRepository = tournamentMatchRepository;
        this.matchOperation = matchOperation;
        this.deadlineHours = deadlineHours;
    }

    @Override
    @Transactional
    public int processExpiredConfirmationDeadlines() {
        Instant deadline = Instant.now().minus(Duration.ofHours(deadlineHours));
        List<TournamentMatch> expiredMatches = tournamentMatchRepository.findExpiredUnconfirmedMatches(
                TournamentStatus.IN_PROGRESS,
                TournamentMatchStatus.IN_PROGRESS,
                Match.STATUS_PENDING_APPROVAL,
                Match.STATUS_PARTIALLY_CONFIRMED,
                deadline
        );

        int processed = 0;
        for (TournamentMatch tm : expiredMatches) {
            try {
                Match coreMatch = tm.getMatch();
                if (coreMatch != null) {
                    coreMatch.autoConfirmBySystem();
                    matchOperation.saveMatch(coreMatch);
                    log.info("Auto-confirmed expired tournament match: matchId={}, tournamentMatchId={}, tournamentId={}, deadlineHours={}",
                            coreMatch.getId(), tm.getId(), tm.getTournament() != null ? tm.getTournament().getId() : null, deadlineHours);
                    processed++;
                }
            } catch (Exception e) {
                log.warn("Failed to auto-confirm expired tournament match: tournamentMatchId={}", tm.getId(), e);
            }
        }
        return processed;
    }
}
