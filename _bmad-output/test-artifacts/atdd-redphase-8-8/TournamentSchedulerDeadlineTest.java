package com.tictactore.scheduler;

import com.tictactore.repository.TournamentRepository;
import com.tictactore.service.tournament.TournamentConfirmationDeadlineService;
import com.tictactore.service.tournament.TournamentLifecycleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentScheduler Deadline Check ATDD Unit Tests")
class TournamentSchedulerDeadlineTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TournamentLifecycleService tournamentLifecycleService;

    @Mock
    private TournamentConfirmationDeadlineService tournamentConfirmationDeadlineService;

    @InjectMocks
    private TournamentScheduler scheduler;

    @Test
    @DisplayName("[P0] Should invoke processExpiredConfirmationDeadlines on scheduler tick")
    void shouldInvokeProcessExpiredConfirmationDeadlines() {
        when(tournamentConfirmationDeadlineService.processExpiredConfirmationDeadlines()).thenReturn(2);

        scheduler.checkConfirmationDeadlines();

        verify(tournamentConfirmationDeadlineService).processExpiredConfirmationDeadlines();
    }

    @Test
    @DisplayName("[P1] Should catch and log exceptions from confirmation deadline service without propagating")
    void shouldCatchException_whenDeadlineServiceThrows() {
        doThrow(new RuntimeException("Database error"))
                .when(tournamentConfirmationDeadlineService).processExpiredConfirmationDeadlines();

        scheduler.checkConfirmationDeadlines();

        verify(tournamentConfirmationDeadlineService).processExpiredConfirmationDeadlines();
    }
}
