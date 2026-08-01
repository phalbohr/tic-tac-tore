package com.tictactore.service;

import com.tictactore.exception.InvalidMatchStateException;
import com.tictactore.exception.UnauthorizedMatchActionException;
import com.tictactore.model.Match;
import com.tictactore.repository.MatchRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
