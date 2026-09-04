package com.tictactore.scheduler;

import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentStatus;
import com.tictactore.repository.TournamentRepository;
import com.tictactore.service.tournament.TournamentConfirmationDeadlineService;
import com.tictactore.service.tournament.TournamentLifecycleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentScheduler Tests")
class TournamentSchedulerTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TournamentLifecycleService tournamentLifecycleService;

    @Mock
    private TournamentConfirmationDeadlineService tournamentConfirmationDeadlineService;

    @InjectMocks
    private TournamentScheduler scheduler;

    @Test
    void shouldFindAndStartExpiredRegistrationTournaments() {
        UUID t1 = UUID.randomUUID();
        UUID t2 = UUID.randomUUID();

        Tournament tournament1 = Tournament.builder().id(t1).status(TournamentStatus.REGISTRATION_OPEN).build();
        Tournament tournament2 = Tournament.builder().id(t2).status(TournamentStatus.REGISTRATION_OPEN).build();

        when(tournamentRepository.findByStatusAndRegistrationDeadlineLessThanEqual(
                eq(TournamentStatus.REGISTRATION_OPEN),
                any(Instant.class)
        )).thenReturn(List.of(tournament1, tournament2));

        scheduler.checkAndStartTournaments();

        verify(tournamentLifecycleService).startTournament(t1);
        verify(tournamentLifecycleService).startTournament(t2);
    }

    @Test
    void shouldInvokeProcessExpiredConfirmationDeadlines() {
        when(tournamentConfirmationDeadlineService.processExpiredConfirmationDeadlines()).thenReturn(2);

        scheduler.checkConfirmationDeadlines();

        verify(tournamentConfirmationDeadlineService).processExpiredConfirmationDeadlines();
    }

    @Test
    void shouldCatchException_whenDeadlineServiceThrows() {
        doThrow(new RuntimeException("Database error"))
                .when(tournamentConfirmationDeadlineService).processExpiredConfirmationDeadlines();

        scheduler.checkConfirmationDeadlines();

        verify(tournamentConfirmationDeadlineService).processExpiredConfirmationDeadlines();
    }
}
