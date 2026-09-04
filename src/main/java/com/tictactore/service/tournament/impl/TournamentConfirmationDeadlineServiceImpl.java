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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class TournamentConfirmationDeadlineServiceImpl implements TournamentConfirmationDeadlineService {

    private static final int DEFAULT_BATCH_SIZE = 100;

    private final TournamentMatchRepository tournamentMatchRepository;
    private final MatchOperation matchOperation;
    private final int deadlineHours;

    public TournamentConfirmationDeadlineServiceImpl(
            TournamentMatchRepository tournamentMatchRepository,
            MatchOperation matchOperation,
            @Value("${app.tournament.confirmation-deadline-hours:48}") int deadlineHours
    ) {
        if (deadlineHours <= 0) {
            throw new IllegalArgumentException("Deadline hours must be greater than 0, got: " + deadlineHours);
        }
        this.tournamentMatchRepository = Objects.requireNonNull(tournamentMatchRepository, "tournamentMatchRepository must not be null");
        this.matchOperation = Objects.requireNonNull(matchOperation, "matchOperation must not be null");
        this.deadlineHours = deadlineHours;
    }

    @Override
    public int processExpiredConfirmationDeadlines() {
        Instant deadline = Instant.now().minus(Duration.ofHours(deadlineHours));
        Pageable pageable = PageRequest.of(0, DEFAULT_BATCH_SIZE);
        List<TournamentMatch> expiredMatches = tournamentMatchRepository.findExpiredUnconfirmedMatches(
                TournamentStatus.IN_PROGRESS,
                TournamentMatchStatus.IN_PROGRESS,
                Match.STATUS_PENDING_APPROVAL,
                Match.STATUS_PARTIALLY_CONFIRMED,
                deadline,
                pageable
        );

        int processed = 0;
        for (TournamentMatch tm : expiredMatches) {
            try {
                Match coreMatch = tm.getMatch();
                coreMatch.autoConfirmBySystem();
                matchOperation.saveMatch(coreMatch);
                List<UUID> unresponsiveParticipantIds = coreMatch.getOpponentIds().stream()
                        .filter(id -> !coreMatch.hasConfirmed(id))
                        .toList();
                log.info("Auto-confirmed expired tournament match: matchId={}, tournamentMatchId={}, tournamentId={}, participantIds={}, unresponsiveParticipantIds={}, deadlineHours={}",
                        coreMatch.getId(), tm.getId(), tm.getTournament().getId(), coreMatch.getParticipantIds(), unresponsiveParticipantIds, deadlineHours);
                processed++;
            } catch (Exception e) {
                log.warn("Failed to auto-confirm expired tournament match: tournamentMatchId={}, error={}", tm.getId(), e.getMessage());
            }
        }
        return processed;
    }
}
