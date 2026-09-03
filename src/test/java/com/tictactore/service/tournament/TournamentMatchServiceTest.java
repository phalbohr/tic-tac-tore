package com.tictactore.service.tournament;

import com.tictactore.event.TournamentMatchCancelledEvent;
import com.tictactore.event.TournamentMatchStartedEvent;
import com.tictactore.exception.InvalidMatchStateException;
import com.tictactore.exception.ParticipantBusyException;
import com.tictactore.exception.UnauthorizedMatchActionException;
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
import com.tictactore.repository.TournamentRepository;
import com.tictactore.service.tournament.impl.TournamentMatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentMatchService Unit Specifications")
class TournamentMatchServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TournamentMatchRepository tournamentMatchRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TournamentStandingsService tournamentStandingsService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TournamentMatchServiceImpl tournamentMatchService;

    private UUID tournamentId;
    private UUID matchId;
    private UUID user1Id;
    private UUID user2Id;
    private UUID partner1Id;
    private UUID partner2Id;

    private Tournament tournament;
    private TournamentRegistration reg1;
    private TournamentRegistration reg2;
    private TournamentRegistration reg1Partner;
    private TournamentRegistration reg2Partner;
    private TournamentMatch tournamentMatch;

    @BeforeEach
    void setUp() {
        tournamentId = UUID.randomUUID();
        matchId = UUID.randomUUID();
        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();
        partner1Id = UUID.randomUUID();
        partner2Id = UUID.randomUUID();

        tournament = Tournament.builder()
                .id(tournamentId)
                .name("Spring Cup 2026")
                .format(TournamentFormat.CHAMPIONSHIP)
                .status(TournamentStatus.IN_PROGRESS)
                .build();

        var user1 = User.builder().id(user1Id).nickname("Alice").build();
        var user2 = User.builder().id(user2Id).nickname("Bob").build();
        var partner1 = User.builder().id(partner1Id).nickname("Charlie").build();
        var partner2 = User.builder().id(partner2Id).nickname("David").build();

        reg1 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(user1).partner(partner1).seed(1).build();
        reg2 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(user2).partner(partner2).seed(4).build();
        reg1Partner = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(partner1).build();
        reg2Partner = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(partner2).build();

        tournamentMatch = TournamentMatch.builder()
                .id(matchId)
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .seed1(1)
                .seed2(4)
                .status(TournamentMatchStatus.READY)
                .build();
    }

    @Nested
    @DisplayName("Start Match Specifications (AC1, AC3)")
    class StartMatchSpecs {

        @Test
        void shouldStartMatch_whenParticipantsAvailable() {
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));
            when(tournamentMatchRepository.findActiveMatchesForParticipants(eq(tournamentId), eq(TournamentMatchStatus.IN_PROGRESS), any()))
                    .thenReturn(Collections.emptyList());
            when(tournamentMatchRepository.save(any(TournamentMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = tournamentMatchService.startMatch(tournamentId, matchId, user1Id);

            assertThat(response).isNotNull();
            assertThat(tournamentMatch.getStatus()).isEqualTo(TournamentMatchStatus.IN_PROGRESS);
            var eventCaptor = ArgumentCaptor.forClass(TournamentMatchStartedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().matchId()).isEqualTo(matchId);
            assertThat(eventCaptor.getValue().tournamentId()).isEqualTo(tournamentId);
        }

        @Test
        void shouldStartMatch_whenInvokedByDoublesPartner() {
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));
            when(tournamentMatchRepository.findActiveMatchesForParticipants(eq(tournamentId), eq(TournamentMatchStatus.IN_PROGRESS), any()))
                    .thenReturn(Collections.emptyList());
            when(tournamentMatchRepository.save(any(TournamentMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = tournamentMatchService.startMatch(tournamentId, matchId, partner1Id);

            assertThat(response).isNotNull();
            assertThat(tournamentMatch.getStatus()).isEqualTo(TournamentMatchStatus.IN_PROGRESS);
        }

        @Test
        void shouldThrowParticipantBusyException_whenParticipantIsInActiveMatch() {
            var busyActiveMatch = TournamentMatch.builder()
                    .id(UUID.randomUUID())
                    .tournament(tournament)
                    .participant1(reg2)
                    .status(TournamentMatchStatus.IN_PROGRESS)
                    .build();

            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));
            when(tournamentMatchRepository.findActiveMatchesForParticipants(eq(tournamentId), eq(TournamentMatchStatus.IN_PROGRESS), any()))
                    .thenReturn(List.of(busyActiveMatch));

            assertThatThrownBy(() -> tournamentMatchService.startMatch(tournamentId, matchId, user1Id))
                    .isInstanceOf(ParticipantBusyException.class)
                    .hasMessageContaining("Bob");
        }

        @Test
        void shouldThrowParticipantBusyException_whenPartnerIsBusy() {
            tournamentMatch.setParticipant1Partner(reg1Partner);
            tournamentMatch.setParticipant2Partner(reg2Partner);

            var busyPartnerMatch = TournamentMatch.builder()
                    .id(UUID.randomUUID())
                    .tournament(tournament)
                    .participant1(reg1Partner)
                    .status(TournamentMatchStatus.IN_PROGRESS)
                    .build();

            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));
            when(tournamentMatchRepository.findActiveMatchesForParticipants(eq(tournamentId), eq(TournamentMatchStatus.IN_PROGRESS), any()))
                    .thenReturn(List.of(busyPartnerMatch));

            assertThatThrownBy(() -> tournamentMatchService.startMatch(tournamentId, matchId, user1Id))
                    .isInstanceOf(ParticipantBusyException.class)
                    .hasMessageContaining("Charlie");
        }

        @Test
        void shouldThrowException_whenTournamentNotInProgress() {
            tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

            assertThatThrownBy(() -> tournamentMatchService.startMatch(tournamentId, matchId, user1Id))
                    .isInstanceOf(InvalidMatchStateException.class)
                    .hasMessageContaining("Tournament is not in progress");
        }

        @Test
        void shouldThrowException_whenMatchAlreadyCompleted() {
            tournamentMatch.setStatus(TournamentMatchStatus.COMPLETED);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));

            assertThatThrownBy(() -> tournamentMatchService.startMatch(tournamentId, matchId, user1Id))
                    .isInstanceOf(InvalidMatchStateException.class)
                    .hasMessageContaining("Match cannot be started");
        }

        @Test
        void shouldThrowUnauthorized_whenNonParticipantStartsMatch() {
            var strangerId = UUID.randomUUID();
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));

            assertThatThrownBy(() -> tournamentMatchService.startMatch(tournamentId, matchId, strangerId))
                    .isInstanceOf(UnauthorizedMatchActionException.class);
        }
    }

    @Nested
    @DisplayName("Cancel Match Specifications (AC5)")
    class CancelMatchSpecs {

        @Test
        void shouldRevertMatchToReady_whenCancelled() {
            tournamentMatch.setStatus(TournamentMatchStatus.IN_PROGRESS);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));
            when(tournamentMatchRepository.save(any(TournamentMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = tournamentMatchService.cancelMatch(tournamentId, matchId, user1Id);

            assertThat(response).isNotNull();
            assertThat(tournamentMatch.getStatus()).isEqualTo(TournamentMatchStatus.READY);
            var eventCaptor = ArgumentCaptor.forClass(TournamentMatchCancelledEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().matchId()).isEqualTo(matchId);
            assertThat(eventCaptor.getValue().cancelledByUserId()).isEqualTo(user1Id);
        }

        @Test
        void shouldAllowDoublesPartnerToCancelMatch() {
            tournamentMatch.setStatus(TournamentMatchStatus.IN_PROGRESS);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));
            when(tournamentMatchRepository.save(any(TournamentMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = tournamentMatchService.cancelMatch(tournamentId, matchId, partner1Id);

            assertThat(response).isNotNull();
            assertThat(tournamentMatch.getStatus()).isEqualTo(TournamentMatchStatus.READY);
        }

        @Test
        void shouldThrowException_whenCancellingNonInProgressMatch() {
            tournamentMatch.setStatus(TournamentMatchStatus.READY);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));

            assertThatThrownBy(() -> tournamentMatchService.cancelMatch(tournamentId, matchId, user1Id))
                    .isInstanceOf(InvalidMatchStateException.class)
                    .hasMessageContaining("Only in-progress matches can be cancelled");
        }

        @Test
        void shouldThrowException_whenTournamentNotInProgressOnCancel() {
            tournament.setStatus(TournamentStatus.COMPLETED);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

            assertThatThrownBy(() -> tournamentMatchService.cancelMatch(tournamentId, matchId, user1Id))
                    .isInstanceOf(InvalidMatchStateException.class)
                    .hasMessageContaining("Tournament is not in progress");
        }
    }

    @Nested
    @DisplayName("Complete Match Specifications (AC2, AC6)")
    class CompleteMatchSpecs {

        @Test
        void shouldCompleteMatch_andAdvanceWinnerInCupWithPropagatedSeed() {
            tournament.setFormat(TournamentFormat.CUP);
            var nextMatch = TournamentMatch.builder()
                    .id(UUID.randomUUID())
                    .tournament(tournament)
                    .round(2)
                    .matchOrder(1)
                    .participant2(reg2)
                    .seed2(4)
                    .status(TournamentMatchStatus.PENDING)
                    .build();
            tournamentMatch.setNextMatch(nextMatch);
            tournamentMatch.setMatchOrder(1);

            var coreMatchId = UUID.randomUUID();
            var coreMatch = Match.builder()
                    .id(coreMatchId)
                    .teamAAttackerId(user2Id)
                    .teamBAttackerId(user1Id)
                    .games(List.of(
                            Game.builder().gameOrder(1).teamAScore(10).teamBScore(4).build(),
                            Game.builder().gameOrder(2).teamAScore(10).teamBScore(5).build()
                    ))
                    .build();

            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));
            when(matchRepository.findById(coreMatchId)).thenReturn(Optional.of(coreMatch));

            tournamentMatchService.completeMatch(matchId, coreMatchId);

            assertThat(tournamentMatch.getStatus()).isEqualTo(TournamentMatchStatus.COMPLETED);
            assertThat(tournamentMatch.getWinner()).isEqualTo(reg2);
            assertThat(nextMatch.getParticipant1()).isEqualTo(reg2);
            assertThat(nextMatch.getSeed1()).isEqualTo(4);
            assertThat(nextMatch.getStatus()).isEqualTo(TournamentMatchStatus.READY);
            verify(tournamentMatchRepository).save(tournamentMatch);
            verify(tournamentMatchRepository).save(nextMatch);
        }

        @Test
        void shouldNotOverwriteNextMatch_whenNextMatchAlreadyStartedOrCompleted() {
            tournament.setFormat(TournamentFormat.CUP);
            var nextMatch = TournamentMatch.builder()
                    .id(UUID.randomUUID())
                    .tournament(tournament)
                    .round(2)
                    .matchOrder(1)
                    .participant1(reg1)
                    .participant2(reg2)
                    .status(TournamentMatchStatus.IN_PROGRESS)
                    .build();
            tournamentMatch.setNextMatch(nextMatch);
            tournamentMatch.setMatchOrder(1);

            var coreMatchId = UUID.randomUUID();
            var coreMatch = Match.builder()
                    .id(coreMatchId)
                    .teamAAttackerId(user1Id)
                    .teamBAttackerId(user2Id)
                    .games(List.of(
                            Game.builder().gameOrder(1).teamAScore(10).teamBScore(4).build()
                    ))
                    .build();

            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));
            when(matchRepository.findById(coreMatchId)).thenReturn(Optional.of(coreMatch));

            tournamentMatchService.completeMatch(matchId, coreMatchId);

            assertThat(tournamentMatch.getStatus()).isEqualTo(TournamentMatchStatus.COMPLETED);
            assertThat(tournamentMatch.getWinner()).isEqualTo(reg1);
            verify(tournamentMatchRepository, never()).save(nextMatch);
        }

        @Test
        void shouldHandleTie_bySettingWinnerToNull() {
            var coreMatchId = UUID.randomUUID();
            var coreMatch = Match.builder()
                    .id(coreMatchId)
                    .teamAAttackerId(user1Id)
                    .teamBAttackerId(user2Id)
                    .games(List.of(
                            Game.builder().gameOrder(1).teamAScore(10).teamBScore(5).build(),
                            Game.builder().gameOrder(2).teamAScore(5).teamBScore(10).build()
                    ))
                    .build();

            when(tournamentMatchRepository.findById(matchId)).thenReturn(Optional.of(tournamentMatch));
            when(matchRepository.findById(coreMatchId)).thenReturn(Optional.of(coreMatch));

            tournamentMatchService.completeMatch(matchId, coreMatchId);

            assertThat(tournamentMatch.getStatus()).isEqualTo(TournamentMatchStatus.COMPLETED);
            assertThat(tournamentMatch.getWinner()).isNull();
        }
    }
}
