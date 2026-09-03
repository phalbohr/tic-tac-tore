package com.tictactore.service.tournament;

import com.tictactore.dto.tournament.TournamentStandingResponse;
import com.tictactore.event.TournamentCompletedEvent;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRepository;
import com.tictactore.service.tournament.impl.TournamentMatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentMatchServiceImpl Completion Tests")
class TournamentMatchServiceCompletionTest {

    @Mock
    private TournamentMatchRepository tournamentMatchRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TournamentStandingsService tournamentStandingsService;

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
                .name("Cup 2026")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
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
                .status(TournamentMatchStatus.READY)
                .build();

        when(tournamentMatchRepository.findById(finalMatchId)).thenReturn(Optional.of(finalMatch));

        tournamentMatchService.completeMatch(finalMatchId, null);

        verify(tournamentRepository).save(tournament);
        assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.COMPLETED);

        var eventCaptor = ArgumentCaptor.forClass(TournamentCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        var publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.tournamentId()).isEqualTo(tournamentId);
        assertThat(publishedEvent.winnerRegistrationId()).isEqualTo(regAlice.getId());
    }

    @Test
    @DisplayName("Should mark tournament as COMPLETED and publish TournamentCompletedEvent when all CHAMPIONSHIP matches complete")
    void shouldCompleteChampionshipTournamentWhenAllMatchesConcluded() {
        var champTournament = Tournament.builder()
                .id(tournamentId)
                .name("League 2026")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .status(TournamentStatus.IN_PROGRESS)
                .build();

        var matchId = UUID.randomUUID();
        var lastMatch = TournamentMatch.builder()
                .id(matchId)
                .tournament(champTournament)
                .participant1(regAlice)
                .participant2(regBob)
                .winner(regAlice)
                .status(TournamentMatchStatus.READY)
                .build();

        var otherCompletedMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(champTournament)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(lastMatch));
        when(tournamentMatchRepository.findByTournamentId(tournamentId)).thenReturn(List.of(lastMatch, otherCompletedMatch));
        when(tournamentStandingsService.calculateStandings(tournamentId)).thenReturn(List.of(
                new TournamentStandingResponse(regAlice.getId(), regAlice.getPlayer().getId(), "Alice", null, null, null, null, 2, 2, 0, 4, 0, 4, 6, false, 1)
        ));

        tournamentMatchService.completeMatch(matchId, null);

        verify(tournamentRepository).save(champTournament);
        assertThat(champTournament.getStatus()).isEqualTo(TournamentStatus.COMPLETED);

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
                .status(TournamentMatchStatus.READY)
                .build();

        when(tournamentMatchRepository.findById(semiMatchId)).thenReturn(Optional.of(semiMatch));

        tournamentMatchService.completeMatch(semiMatchId, null);

        verify(eventPublisher, never()).publishEvent(any(TournamentCompletedEvent.class));
        assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.IN_PROGRESS);
    }
}
