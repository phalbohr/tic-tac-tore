package com.tictactore.scheduler;

import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentStatus;
import com.tictactore.repository.TournamentRepository;
import com.tictactore.service.tournament.TournamentLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TournamentScheduler {

    private final TournamentRepository tournamentRepository;
    private final TournamentLifecycleService tournamentLifecycleService;

    @Scheduled(fixedDelayString = "${app.tournament.scheduler-interval-ms:60000}")
    public void checkAndStartTournaments() {
        Instant now = Instant.now();
        List<Tournament> expiredRegistrationTournaments =
                tournamentRepository.findByStatusAndRegistrationDeadlineLessThanEqual(
                        TournamentStatus.REGISTRATION_OPEN,
                        now
                );

        if (expiredRegistrationTournaments.isEmpty()) {
            return;
        }

        log.info("Found {} tournaments past registration deadline to start/cancel",
                expiredRegistrationTournaments.size());

        for (Tournament tournament : expiredRegistrationTournaments) {
            try {
                tournamentLifecycleService.startTournament(tournament.getId());
            } catch (Exception e) {
                log.error("Failed to auto-process tournament {} lifecycle", tournament.getId(), e);
            }
        }
    }
}
