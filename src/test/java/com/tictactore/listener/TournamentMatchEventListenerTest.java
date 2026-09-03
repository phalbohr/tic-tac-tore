package com.tictactore.listener;

import com.tictactore.event.MatchConfirmedEvent;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.service.tournament.TournamentStandingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentMatchEventListener Specifications")
class TournamentMatchEventListenerTest {

    @Mock
    private TournamentMatchRepository tournamentMatchRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TournamentStandingsService tournamentStandingsService;

    @InjectMocks
    private TournamentMatchEventListener listener;

    private UUID matchId;
    private UUID tournamentId;
    private Tournament tournament;
    private TournamentRegistration reg1;
    private TournamentRegistration reg2;
    private TournamentMatch tournamentMatch;
    private Match match;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        tournamentId = UUID.randomUUID();

        tournament = Tournament.builder()
                .id(tournamentId)
                .name("Cup Knockout 2026")
                .format(TournamentFormat.CUP)
                .status(TournamentStatus.IN_PROGRESS)
                .build();

        var player1 = User.builder().id(UUID.randomUUID()).nickname("Winner").build();
        var player2 = User.builder().id(UUID.randomUUID()).nickname("Loser").build();

        reg1 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(player1).build();
        reg2 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(player2).build();

        tournamentMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.IN_PROGRESS)
                .build();

        var games = new ArrayList<Game>();
        games.add(Game.builder().gameOrder(1).teamAScore(10).teamBScore(5).build());
        games.add(Game.builder().gameOrder(2).teamAScore(10).teamBScore(8).build());

        match = Match.builder()
                .id(matchId)
                .teamAAttackerId(player1.getId())
                .teamBAttackerId(player2.getId())
                .status("CONFIRMED")
                .games(games)
                .build();
    }

    @Test
    void shouldCompleteTournamentMatch_whenMatchConfirmed() {
        when(tournamentMatchRepository.findByMatchId(matchId)).thenReturn(Optional.of(tournamentMatch));
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        listener.handleMatchConfirmed(new MatchConfirmedEvent(matchId, List.of(reg1.getPlayer().getId(), reg2.getPlayer().getId())));

        assertThat(tournamentMatch.getStatus()).isEqualTo(TournamentMatchStatus.COMPLETED);
        assertThat(tournamentMatch.getWinner()).isEqualTo(reg1);
        assertThat(tournamentMatch.getMatch()).isEqualTo(match);
        verify(tournamentMatchRepository).save(tournamentMatch);
        verify(tournamentStandingsService).calculateStandings(tournamentId);
    }

    @Test
    void shouldAdvanceWinnerToNextMatch_inCupFormat() {
        var nextMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .round(2)
                .matchOrder(1)
                .participant2(reg2)
                .status(TournamentMatchStatus.PENDING)
                .build();
        tournamentMatch.setNextMatch(nextMatch);
        tournamentMatch.setMatchOrder(1);

        when(tournamentMatchRepository.findByMatchId(matchId)).thenReturn(Optional.of(tournamentMatch));
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        listener.handleMatchConfirmed(new MatchConfirmedEvent(matchId, List.of(reg1.getPlayer().getId(), reg2.getPlayer().getId())));

        assertThat(nextMatch.getParticipant1()).isEqualTo(reg1);
        assertThat(nextMatch.getStatus()).isEqualTo(TournamentMatchStatus.READY);
        verify(tournamentMatchRepository).save(nextMatch);
    }

    @Test
    void shouldAdvanceWinner_withoutSettingReady_whenOtherSlotNull() {
        var nextMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .round(2)
                .matchOrder(1)
                .participant1(null)
                .participant2(null)
                .status(TournamentMatchStatus.PENDING)
                .build();
        tournamentMatch.setNextMatch(nextMatch);
        tournamentMatch.setMatchOrder(1);

        when(tournamentMatchRepository.findByMatchId(matchId)).thenReturn(Optional.of(tournamentMatch));
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        listener.handleMatchConfirmed(new MatchConfirmedEvent(matchId, List.of(reg1.getPlayer().getId(), reg2.getPlayer().getId())));

        assertThat(nextMatch.getParticipant1()).isEqualTo(reg1);
        assertThat(nextMatch.getStatus()).isEqualTo(TournamentMatchStatus.PENDING);
        verify(tournamentMatchRepository).save(nextMatch);
    }

    @Test
    void shouldIgnoreEvent_whenNotTournamentMatch() {
        when(tournamentMatchRepository.findByMatchId(matchId)).thenReturn(Optional.empty());

        listener.handleMatchConfirmed(new MatchConfirmedEvent(matchId, List.of(UUID.randomUUID())));

        verify(tournamentMatchRepository, never()).save(any());
        verify(tournamentStandingsService, never()).calculateStandings(any());
    }
}
