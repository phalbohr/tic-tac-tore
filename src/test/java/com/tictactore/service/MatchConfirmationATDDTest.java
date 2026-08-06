package com.tictactore.service;

import com.tictactore.exception.InvalidMatchStateException;
import com.tictactore.exception.UnauthorizedMatchActionException;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.impl.MatchServiceImpl;
import com.tictactore.service.operation.MatchOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ATDD Red-Phase Scaffolds for Match Opponent Confirmation Service Logic.
 * Story 3.2: Single-tap Confirmation with Undo Window
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchConfirmation Service ATDD Specifications")
class MatchConfirmationATDDTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchOperation matchOperation;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private MatchServiceImpl matchService;

    private UUID matchId;
    private UUID creatorId;
    private UUID opponentAttackerId;
    private UUID opponentDefenderId;
    private UUID nonParticipantId;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        creatorId = UUID.randomUUID();
        opponentAttackerId = UUID.randomUUID();
        opponentDefenderId = UUID.randomUUID();
        nonParticipantId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Match Opponent Confirmation Specifications")
    class ConfirmationSpecs {

        @Test
        @DisplayName("[P0] Should set match status to CONFIRMED and record confirmedByUserId and confirmedAt when valid opponent confirms")
        void shouldConfirmMatchSuccessfully() {
            Match pendingMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentAttackerId)
                    .teamBDefenderId(opponentDefenderId)
                    .status("PENDING_APPROVAL")
                    .build();

            Match confirmedMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentAttackerId)
                    .teamBDefenderId(opponentDefenderId)
                    .status("CONFIRMED")
                    .confirmedByUserId(opponentAttackerId)
                    .confirmedAt(Instant.now())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentAttackerId))).thenReturn(confirmedMatch);

            var response = matchService.confirmMatch(matchId, opponentAttackerId, "idempotency-key-1");

            assertThat(response.status()).isEqualTo("CONFIRMED");
            assertThat(response.confirmedByUserId()).isEqualTo(opponentAttackerId);
            verify(matchOperation).confirmMatch(pendingMatch, opponentAttackerId);
        }

        @Test
        @DisplayName("[P0] Should throw UnauthorizedMatchActionException when creator attempts self-confirmation")
        void shouldRejectCreatorSelfConfirmation() {
            Match pendingMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentAttackerId)
                    .teamBDefenderId(opponentDefenderId)
                    .status("PENDING_APPROVAL")
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
            when(matchOperation.confirmMatch(any(Match.class), eq(creatorId)))
                    .thenThrow(new UnauthorizedMatchActionException("User " + creatorId + " is not an opponent for match " + matchId));

            assertThatThrownBy(() -> matchService.confirmMatch(matchId, creatorId, "idempotency-key-2"))
                    .isInstanceOf(UnauthorizedMatchActionException.class);
        }

        @Test
        @DisplayName("[P0] Should throw UnauthorizedMatchActionException when user is not an opponent in the match")
        void shouldRejectNonOpponentConfirmation() {
            Match pendingMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentAttackerId)
                    .teamBDefenderId(opponentDefenderId)
                    .status("PENDING_APPROVAL")
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
            when(matchOperation.confirmMatch(any(Match.class), eq(nonParticipantId)))
                    .thenThrow(new UnauthorizedMatchActionException("User " + nonParticipantId + " is not an opponent for match " + matchId));

            assertThatThrownBy(() -> matchService.confirmMatch(matchId, nonParticipantId, "idempotency-key-3"))
                    .isInstanceOf(UnauthorizedMatchActionException.class);
        }

        @Test
        @DisplayName("[P1] Should throw InvalidMatchStateException when match is not in PENDING_APPROVAL status")
        void shouldRejectConfirmationForNonPendingMatch() {
            Match confirmedMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentAttackerId)
                    .teamBDefenderId(opponentDefenderId)
                    .status("CONFIRMED")
                    .confirmedByUserId(opponentAttackerId)
                    .confirmedAt(Instant.now())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(confirmedMatch));

            assertThatThrownBy(() -> matchService.confirmMatch(matchId, nonParticipantId, "idempotency-key-4"))
                    .isInstanceOf(InvalidMatchStateException.class);
        }
    }

    @Nested
    @DisplayName("Context-Aware Multi-Confirmation Specifications")
    class ContextAwareConfirmationSpecs {

        @Test
        @DisplayName("[P0] AC1: 1v1 participant confirms -> CONFIRMED immediately")
        void ac1_shouldConfirmMatch1v1Participant() {
            Match pendingMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamBAttackerId(opponentAttackerId)
                    .status("PENDING_APPROVAL")
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            Match confirmedMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamBAttackerId(opponentAttackerId)
                    .status("CONFIRMED")
                    .confirmedByOpponentIds(opponentAttackerId.toString())
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentAttackerId))).thenReturn(confirmedMatch);

            var response = matchService.confirmMatch(matchId, opponentAttackerId, "idempotency-key-5");

            assertThat(response.status()).isEqualTo("CONFIRMED");
            verify(matchOperation).confirmMatch(pendingMatch, opponentAttackerId);
        }

        @Test
        @DisplayName("[P0] AC2: 1v1 referee first confirm -> stays PENDING_APPROVAL (not CONFIRMED)")
        void ac2_shouldNotConfirmWhen1v1RefereeFirstOpponentConfirms() {
            Match pendingMatch = Match.builder()
                    .id(matchId)
                    .creatorId(UUID.randomUUID())
                    .teamAAttackerId(opponentAttackerId)
                    .teamADefenderId(null)
                    .teamBAttackerId(opponentDefenderId)
                    .teamBDefenderId(null)
                    .status("PENDING_APPROVAL")
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            Match firstConfirmedMatch = Match.builder()
                    .id(matchId)
                    .creatorId(pendingMatch.getCreatorId())
                    .teamAAttackerId(opponentAttackerId)
                    .teamBAttackerId(opponentDefenderId)
                    .status("PENDING_APPROVAL")
                    .confirmedByOpponentIds(opponentAttackerId.toString())
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentAttackerId))).thenReturn(firstConfirmedMatch);

            var response = matchService.confirmMatch(matchId, opponentAttackerId, "idempotency-key-6");

            assertThat(response.status()).isEqualTo("PENDING_APPROVAL");
            verify(matchOperation).confirmMatch(pendingMatch, opponentAttackerId);
            verifyNoInteractions(pushNotificationService);
        }

        @Test
        @DisplayName("[P0] AC3: 2v2 standard first confirm -> PARTIALLY_CONFIRMED + notification to remaining opponent")
        void ac3_shouldEnterPartiallyConfirmedAndNotify_when2v2StandardFirstConfirms() {
            Match pendingMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(opponentAttackerId)
                    .teamBAttackerId(opponentDefenderId)
                    .teamBDefenderId(nonParticipantId)
                    .status("PENDING_APPROVAL")
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            Match partiallyConfirmed = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(opponentAttackerId)
                    .teamBAttackerId(opponentDefenderId)
                    .teamBDefenderId(nonParticipantId)
                    .status("PARTIALLY_CONFIRMED")
                    .confirmedByOpponentIds(opponentDefenderId.toString())
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentDefenderId))).thenReturn(partiallyConfirmed);
            when(userRepository.findAllById(List.of(nonParticipantId))).thenReturn(
                    List.of(User.builder().id(nonParticipantId).build()));

            var response = matchService.confirmMatch(matchId, opponentDefenderId, "idempotency-key-7");

            assertThat(response.status()).isEqualTo("PARTIALLY_CONFIRMED");
            assertThat(response.confirmedByOpponentIds()).containsExactly(opponentDefenderId);
            verify(matchOperation).confirmMatch(pendingMatch, opponentDefenderId);
            verify(pushNotificationService).sendPartialConfirmationNotification(any(Match.class), anyList(), anyString());
        }

        @Test
        @DisplayName("[P0] AC4: 2v2 random first confirm -> stays PENDING_APPROVAL (no partial state)")
        void ac4_shouldStayPendingWhen2v2RandomFirstConfirms() {
            Match pendingMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(opponentAttackerId)
                    .teamBAttackerId(opponentDefenderId)
                    .teamBDefenderId(nonParticipantId)
                    .status("PENDING_APPROVAL")
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_RANDOM)
                    .build();

            Match firstConfirmed = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(opponentAttackerId)
                    .teamBAttackerId(opponentDefenderId)
                    .teamBDefenderId(nonParticipantId)
                    .status("PENDING_APPROVAL")
                    .confirmedByOpponentIds(opponentDefenderId.toString())
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_RANDOM)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentDefenderId))).thenReturn(firstConfirmed);

            var response = matchService.confirmMatch(matchId, opponentDefenderId, "idempotency-key-8");

            assertThat(response.status()).isEqualTo("PENDING_APPROVAL");
            verify(matchOperation).confirmMatch(pendingMatch, opponentDefenderId);
            verifyNoInteractions(pushNotificationService);
        }

        @Test
        @DisplayName("[P0] AC5: 2v2 referee needs 1 per team -> CONFIRMED only when both teams represented")
        void ac5_shouldConfirmWhen2v2RefereeHasOnePerTeam() {
            Match pendingMatch = Match.builder()
                    .id(matchId)
                    .creatorId(UUID.randomUUID())
                    .teamAAttackerId(opponentAttackerId)
                    .teamADefenderId(opponentDefenderId)
                    .teamBAttackerId(nonParticipantId)
                    .teamBDefenderId(UUID.randomUUID())
                    .status("PENDING_APPROVAL")
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            Match confirmedMatch = Match.builder()
                    .id(matchId)
                    .creatorId(pendingMatch.getCreatorId())
                    .teamAAttackerId(opponentAttackerId)
                    .teamADefenderId(opponentDefenderId)
                    .teamBAttackerId(nonParticipantId)
                    .teamBDefenderId(UUID.randomUUID())
                    .status("CONFIRMED")
                    .confirmedByOpponentIds(opponentAttackerId + "," + nonParticipantId)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentAttackerId))).thenReturn(confirmedMatch);

            var response = matchService.confirmMatch(matchId, opponentAttackerId, "idempotency-key-9");

            assertThat(response.status()).isEqualTo("CONFIRMED");
            verify(matchOperation).confirmMatch(pendingMatch, opponentAttackerId);
        }

        @Test
        @DisplayName("[P0] AC6: Double confirmation -> idempotency, no error")
        void ac6_shouldBeIdempotentWhenSameOpponentConfirmsAgain() {
            Match partiallyConfirmed = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(opponentAttackerId)
                    .teamBAttackerId(opponentDefenderId)
                    .teamBDefenderId(nonParticipantId)
                    .status("PARTIALLY_CONFIRMED")
                    .confirmedByOpponentIds(opponentDefenderId.toString())
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(partiallyConfirmed));

            var response = matchService.confirmMatch(matchId, opponentDefenderId, "idempotency-key-10");

            assertThat(response.status()).isEqualTo("PARTIALLY_CONFIRMED");
            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P0] AC7: PARTIALLY_CONFIRMED match second opponent confirms -> CONFIRMED")
        void ac7_shouldConfirmFromPartiallyConfirmedWhenSecondOpponentConfirms() {
            Match partiallyConfirmed = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(opponentAttackerId)
                    .teamBAttackerId(opponentDefenderId)
                    .teamBDefenderId(nonParticipantId)
                    .status("PARTIALLY_CONFIRMED")
                    .confirmedByOpponentIds(opponentDefenderId.toString())
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            Match confirmedMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(opponentAttackerId)
                    .teamBAttackerId(opponentDefenderId)
                    .teamBDefenderId(nonParticipantId)
                    .status("CONFIRMED")
                    .confirmedByOpponentIds(opponentDefenderId + "," + nonParticipantId)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(partiallyConfirmed));
            when(matchOperation.confirmMatch(any(Match.class), eq(nonParticipantId))).thenReturn(confirmedMatch);

            var response = matchService.confirmMatch(matchId, nonParticipantId, "idempotency-key-11");

            assertThat(response.status()).isEqualTo("CONFIRMED");
            verify(matchOperation).confirmMatch(partiallyConfirmed, nonParticipantId);
        }
    }
}
