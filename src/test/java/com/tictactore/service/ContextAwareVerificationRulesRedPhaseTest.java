package com.tictactore.service;

import com.tictactore.dto.MatchResponse;
import com.tictactore.model.Match;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Red-phase acceptance test scaffolds for Story 3.4: Context-Aware Verification Rules.
 * <p>
 * These tests are emitted in the TDD RED PHASE. They assert the expected behavior
 * defined by the acceptance criteria (AC1–AC7) and would fail if the implementation
 * were absent. In the current green-phase state they are disabled to avoid duplicate
 * coverage with the active specs in {@link MatchConfirmationATDDTest} and
 * {@link MatchServiceTest}.
 * <p>
 * To activate: remove {@code @Disabled} and run.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Context-Aware Verification Rules — Red-Phase Scaffolds")
class ContextAwareVerificationRulesRedPhaseTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchOperation matchOperation;

    @Mock
    private com.tictactore.service.PushNotificationService pushNotificationService;

    @InjectMocks
    private MatchServiceImpl matchService;

    private UUID matchId;
    private UUID creatorId;
    private UUID opponentA;
    private UUID opponentB;
    private UUID opponentC;
    private UUID opponentD;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        creatorId = UUID.randomUUID();
        opponentA = UUID.randomUUID();
        opponentB = UUID.randomUUID();
        opponentC = UUID.randomUUID();
        opponentD = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Backward Compatibility Scaffolds")
    class BackwardCompatibilityScaffolds {

        @Test
        @DisplayName("[P1] RED: hasConfirmed() falls back to confirmedByUserId when confirmedByOpponentIds is null")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_hasConfirmed_fallsBackToConfirmedByUserId_whenCsvIsNull() {
            Match legacyMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamBAttackerId(opponentA)
                    .status(Match.STATUS_CONFIRMED)
                    .confirmedByUserId(opponentA)
                    .confirmedAt(Instant.now())
                    .build();

            assertThat(legacyMatch.hasConfirmed(opponentA)).isTrue();
            assertThat(legacyMatch.hasConfirmed(creatorId)).isFalse();
        }
    }

    @Nested
    @DisplayName("getPendingMatches Inclusion Scaffolds")
    class PendingMatchesInclusionScaffolds {

        @Test
        @DisplayName("[P0] RED: getPendingMatches includes PARTIALLY_CONFIRMED matches for unconfirmed users")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_getPendingMatches_includesPartiallyConfirmed_forUnconfirmedUser() {
            Match partialMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(opponentA)
                    .teamBAttackerId(opponentB)
                    .teamBDefenderId(opponentC)
                    .status(Match.STATUS_PARTIALLY_CONFIRMED)
                    .confirmedByOpponentIds(opponentA.toString())
                    .createdAt(Instant.now())
                    .build();

            when(matchRepository.findByStatusIn(List.of(Match.STATUS_PENDING_APPROVAL, Match.STATUS_PARTIALLY_CONFIRMED)))
                    .thenReturn(List.of(partialMatch));

            var result = matchService.getPendingMatches(opponentC);

            assertThat(result.count()).isEqualTo(1);
            assertThat(result.matches().get(0).id()).isEqualTo(matchId);
            assertThat(result.matches().get(0).status()).isEqualTo(Match.STATUS_PARTIALLY_CONFIRMED);
        }

        @Test
        @DisplayName("[P0] RED: getPendingMatches excludes users who already confirmed on PARTIALLY_CONFIRMED match")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_getPendingMatches_excludesAlreadyConfirmedUser_onPartiallyConfirmed() {
            Match partialMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(opponentA)
                    .teamBAttackerId(opponentB)
                    .teamBDefenderId(opponentC)
                    .status(Match.STATUS_PARTIALLY_CONFIRMED)
                    .confirmedByOpponentIds(opponentA.toString())
                    .createdAt(Instant.now())
                    .build();

            when(matchRepository.findByStatusIn(List.of(Match.STATUS_PENDING_APPROVAL, Match.STATUS_PARTIALLY_CONFIRMED)))
                    .thenReturn(List.of(partialMatch));

            var result = matchService.getPendingMatches(opponentA);

            assertThat(result.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("DTO Serialization Scaffolds")
    class DtoSerializationScaffolds {

        @Test
        @DisplayName("[P1] RED: MatchResponse includes entryMode and matchFormat from match entity")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_matchResponse_includesEntryModeAndMatchFormat() {
            Match match = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamBAttackerId(opponentA)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_RANDOM)
                    .createdAt(Instant.now())
                    .build();

            MatchResponse response = matchService.createMatch(
                    new com.tictactore.dto.CreateMatchRequest(
                            "idem-ref", creatorId, creatorId, opponentA, null, null,
                            List.of(new com.tictactore.dto.GameDto(10, 8)),
                            Match.ENTRY_MODE_REFEREE, Match.MATCH_FORMAT_RANDOM
                    )
            );

            assertThat(response.entryMode()).isEqualTo(Match.ENTRY_MODE_REFEREE);
            assertThat(response.matchFormat()).isEqualTo(Match.MATCH_FORMAT_RANDOM);
        }

        @Test
        @DisplayName("[P1] RED: MatchResponse includes confirmedByOpponentIds and requiredConfirmations")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_matchResponse_includesConfirmationFields_afterPartialConfirmation() {
            Match pending = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(opponentA)
                    .teamBAttackerId(opponentB)
                    .teamBDefenderId(opponentC)
                    .status(Match.STATUS_PARTIALLY_CONFIRMED)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .confirmedByOpponentIds(opponentB.toString())
                    .createdAt(Instant.now())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pending));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentB))).thenAnswer(inv -> {
                Match m = inv.getArgument(0);
                return Match.builder()
                        .id(m.getId())
                        .creatorId(m.getCreatorId())
                        .teamAAttackerId(m.getTeamAAttackerId())
                        .teamADefenderId(m.getTeamADefenderId())
                        .teamBAttackerId(m.getTeamBAttackerId())
                        .teamBDefenderId(m.getTeamBDefenderId())
                        .status(Match.STATUS_PARTIALLY_CONFIRMED)
                        .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                        .matchFormat(Match.MATCH_FORMAT_STANDARD)
                        .confirmedByOpponentIds(opponentB.toString())
                        .createdAt(Instant.now())
                        .build();
            });

            var response = matchService.confirmMatch(matchId, opponentB, "idem-red");

            assertThat(response.status()).isEqualTo(Match.STATUS_PARTIALLY_CONFIRMED);
            assertThat(response.confirmedByOpponentIds()).containsExactly(opponentB);
            assertThat(response.requiredConfirmations()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Edge-Case Scaffolds")
    class EdgeCaseScaffolds {

        @Test
        @DisplayName("[P1] RED: 2v2 referee with 2 confirmations from same team stays PENDING_APPROVAL")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_2v2Referee_bothFromSameTeam_staysPending() {
            Match pending = Match.builder()
                    .id(matchId)
                    .creatorId(UUID.randomUUID())
                    .teamAAttackerId(opponentA)
                    .teamADefenderId(opponentB)
                    .teamBAttackerId(opponentC)
                    .teamBDefenderId(opponentD)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            Match sameTeamConfirmed = Match.builder()
                    .id(matchId)
                    .creatorId(pending.getCreatorId())
                    .teamAAttackerId(opponentA)
                    .teamADefenderId(opponentB)
                    .teamBAttackerId(opponentC)
                    .teamBDefenderId(opponentD)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .confirmedByOpponentIds(opponentA + "," + opponentB)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pending));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentA))).thenReturn(sameTeamConfirmed);
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentB))).thenReturn(sameTeamConfirmed);

            var first = matchService.confirmMatch(matchId, opponentA, "idem-red-1");
            assertThat(first.status()).isEqualTo(Match.STATUS_PENDING_APPROVAL);

            var second = matchService.confirmMatch(matchId, opponentB, "idem-red-2");
            assertThat(second.status()).isEqualTo(Match.STATUS_PENDING_APPROVAL);
        }

        @Test
        @DisplayName("[P1] RED: 1v1 with null entryMode defaults to PARTICIPANT")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_1v1_nullEntryMode_defaultsToParticipant() {
            Match match = Match.builder()
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamBAttackerId(opponentA)
                    .entryMode(null)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            assertThat(com.tictactore.rules.VerificationRules.getRequiredConfirmations(match)).isEqualTo(1);
        }
    }
}
