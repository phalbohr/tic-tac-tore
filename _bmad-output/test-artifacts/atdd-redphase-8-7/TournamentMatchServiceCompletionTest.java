package com.tictactore.service.tournament;

import com.tictactore.event.TournamentCompletedEvent;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRepository;
import com.tictactore.service.tournament.impl.TournamentMatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("ATDD red phase: Story 8.7 - Tournament completion logic and event publishing")
@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentMatchServiceImpl Completion Tests")
class TournamentMatchServiceCompletionTest {

    @Mock
    private TournamentMatchRepository tournamentMatchRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TournamentMatchServiceImpl tournamentMatchService;

    private UUID tournamentId;
    private Tournament tournament;
    private TournamentRegistration regAlice;
    private TournamentRegistration regBob;

    @BeforeEach
    void setUp() {
        tournamentId = UUID.randomUUID();
        tournament = Tournament.builder()
                .id(tournamentId)
                .title("Cup 2026")
                .format(TournamentFormat.CUP)
                .status(TournamentStatus.IN_PROGRESS)
                .build();

        var alice = User.builder().id(UUID.randomUUID()).nickname("Alice").build();
        var bob = User.builder().id(UUID.randomUUID()).nickname("Bob").build();

        regAlice = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(alice).build();
        regBob = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(bob).build();
    }

    @Test
    @DisplayName("Should mark tournament as COMPLETED and publish TournamentCompletedEvent when CUP final completes")
    void shouldCompleteTournamentAndPublishEventWhenCupFinalCompletes() {
        var finalMatchId = UUID.randomUUID();
        var finalMatch = TournamentMatch.builder()
                .id(finalMatchId)
                .tournament(tournament)
                .participant1(regAlice)
                .participant2(regBob)
                .winner(regAlice)
                .nextMatch(null)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        when(tournamentMatchRepository.findById(finalMatchId)).thenReturn(Optional.of(finalMatch));
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        tournamentMatchService.completeMatch(finalMatchId, UUID.randomUUID());

        verify(tournamentRepository).save(tournament);
        assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.COMPLETED);

        var eventCaptor = ArgumentCaptor.forClass(TournamentCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        var publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.tournamentId()).isEqualTo(tournamentId);
        assertThat(publishedEvent.winnerRegistrationId()).isEqualTo(regAlice.getId());
    }

    @Test
    @DisplayName("Should not complete tournament when non-final match completes")
    void shouldNotCompleteTournamentWhenNonFinalMatchCompletes() {
        var nextMatch = TournamentMatch.builder().id(UUID.randomUUID()).tournament(tournament).build();
        var semiMatchId = UUID.randomUUID();
        var semiMatch = TournamentMatch.builder()
                .id(semiMatchId)
                .tournament(tournament)
                .participant1(regAlice)
                .participant2(regBob)
                .winner(regAlice)
                .nextMatch(nextMatch)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        when(tournamentMatchRepository.findById(semiMatchId)).thenReturn(Optional.of(semiMatch));

        tournamentMatchService.completeMatch(semiMatchId, UUID.randomUUID());

        verify(eventPublisher, never()).publishEvent(any(TournamentCompletedEvent.class));
        assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.IN_PROGRESS);
    }
}
