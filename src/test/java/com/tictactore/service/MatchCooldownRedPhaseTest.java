package com.tictactore.service;

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
import static org.mockito.Mockito.when;

/**
 * Red-phase acceptance test scaffolds for Story 3.5: Publication Rules &
 * 24-hour Cooldown.
 * <p>
 * These tests are emitted in the TDD RED PHASE. They assert the expected
 * behavior
 * defined by the acceptance criteria (AC1–AC5) and would fail if the
 * implementation
 * were absent. In the current green-phase state they are disabled to avoid
 * duplicate
 * coverage with the active specs in {@link MatchServiceTest} and
 * {@link MatchCooldownServiceTest}.
 * <p>
 * To activate: remove {@code @Disabled} and run.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchCooldown — Red-Phase Scaffolds")
class MatchCooldownRedPhaseTest {

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

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        creatorId = UUID.randomUUID();
        opponentA = UUID.randomUUID();
        opponentB = UUID.randomUUID();
    }

    @Nested
    @DisplayName("AC1: First confirm 2v2 standard → PARTIALLY_CONFIRMED + cooldownExpiresAt")
    class FirstConfirm2v2Standard {

        @Test
        @DisplayName("[P0] RED: Should set cooldownExpiresAt to now+24h when 2v2 standard first opponent confirms")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_2v2StandardFirstConfirm_sets24hCooldown() {
            Match pending = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(opponentB)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            Match partiallyConfirmed = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(opponentB)
                    .status(Match.STATUS_PARTIALLY_CONFIRMED)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .confirmedByOpponentIds(opponentA.toString())
                    .cooldownExpiresAt(Instant.now().plusSeconds(24 * 60 * 60))
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pending));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentA))).thenReturn(partiallyConfirmed);

            var response = matchService.confirmMatch(matchId, opponentA, "idem-red-ac1");

            assertThat(response.status()).isEqualTo(Match.STATUS_PARTIALLY_CONFIRMED);
            assertThat(response.cooldownExpiresAt()).isNotNull();
            assertThat(response.cooldownExpiresAt()).isAfter(Instant.now());
            assertThat(response.cooldownExpiresAt()).isBefore(Instant.now().plusSeconds(25 * 60 * 60));
        }
    }

    @Nested
    @DisplayName("AC2: Second confirm during cooldown → CONFIRMED + cooldown cleared")
    class SecondConfirmDuringCooldown {

        @Test
        @DisplayName("[P0] RED: Should clear cooldownExpiresAt and transition to CONFIRMED when second opponent confirms before expiry")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_secondConfirmDuringCooldown_clearsCooldownAndConfirms() {
            Instant cooldownExpiry = Instant.now().plusSeconds(60);
            Match partial = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(opponentB)
                    .status(Match.STATUS_PARTIALLY_CONFIRMED)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .confirmedByOpponentIds(opponentA.toString())
                    .cooldownExpiresAt(cooldownExpiry)
                    .build();

            Match confirmed = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(opponentB)
                    .status(Match.STATUS_CONFIRMED)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .confirmedByOpponentIds(opponentA.toString() + "," + opponentB.toString())
                    .cooldownExpiresAt(null)
                    .confirmedAt(Instant.now())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(partial));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentB))).thenReturn(confirmed);

            var response = matchService.confirmMatch(matchId, opponentB, "idem-red-ac2");

            assertThat(response.status()).isEqualTo(Match.STATUS_CONFIRMED);
            assertThat(response.cooldownExpiresAt()).isNull();
        }
    }

    @Nested
    @DisplayName("AC3: Cooldown expires → auto-publish via scheduled job")
    class CooldownExpiryAutoPublish {

        @Test
        @DisplayName("[P0] RED: Scheduled job transitions expired PARTIALLY_CONFIRMED match to CONFIRMED")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_scheduledJob_autoPublishesExpiredCooldown() {
            Match expired = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(opponentB)
                    .status(Match.STATUS_PARTIALLY_CONFIRMED)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .confirmedByOpponentIds(opponentA.toString())
                    .cooldownExpiresAt(Instant.now().minusSeconds(60))
                    .build();

            when(matchRepository.findByCooldownExpiresAtBeforeAndStatus(any(Instant.class),
                    eq(Match.STATUS_PARTIALLY_CONFIRMED)))
                    .thenReturn(List.of(expired));
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            com.tictactore.service.MatchCooldownService cooldownService = new com.tictactore.service.MatchCooldownService(
                    matchRepository);
            cooldownService.processExpiredCooldowns();

            assertThat(expired.getStatus()).isEqualTo(Match.STATUS_CONFIRMED);
            assertThat(expired.getCooldownExpiresAt()).isNull();
            assertThat(expired.getConfirmedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("AC4: Non-standard match contexts → no cooldown set")
    class NonStandardNoCooldown {

        @Test
        @DisplayName("[P0] RED: 1v1 participant confirm → CONFIRMED immediately, no cooldown")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_1v1Participant_noCooldownSet() {
            Match pending = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(null)
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            Match confirmed = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(null)
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .confirmedByUserId(opponentA)
                    .confirmedAt(Instant.now())
                    .cooldownExpiresAt(null)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pending));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentA))).thenReturn(confirmed);

            var response = matchService.confirmMatch(matchId, opponentA, "idem-red-ac4-1v1");

            assertThat(response.status()).isEqualTo(Match.STATUS_CONFIRMED);
            assertThat(response.cooldownExpiresAt()).isNull();
        }

        @Test
        @DisplayName("[P0] RED: 2v2 RANDOM first confirm → CONFIRMED immediately, no cooldown")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_2v2Random_noCooldownSet() {
            Match pending = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(opponentB)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_RANDOM)
                    .build();

            Match confirmed = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(opponentB)
                    .status(Match.STATUS_CONFIRMED)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_RANDOM)
                    .confirmedByUserId(opponentA)
                    .confirmedAt(Instant.now())
                    .cooldownExpiresAt(null)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pending));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentA))).thenReturn(confirmed);

            var response = matchService.confirmMatch(matchId, opponentA, "idem-red-ac4-random");

            assertThat(response.status()).isEqualTo(Match.STATUS_CONFIRMED);
            assertThat(response.cooldownExpiresAt()).isNull();
        }

        @Test
        @DisplayName("[P0] RED: 2v2 REFEREE first confirm → CONFIRMED immediately, no cooldown")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_2v2Referee_noCooldownSet() {
            Match pending = Match.builder()
                    .id(matchId)
                    .creatorId(UUID.randomUUID())
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(opponentB)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            Match confirmed = Match.builder()
                    .id(matchId)
                    .creatorId(UUID.randomUUID())
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(opponentB)
                    .status(Match.STATUS_CONFIRMED)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .confirmedByUserId(opponentA)
                    .confirmedAt(Instant.now())
                    .cooldownExpiresAt(null)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(pending));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentA))).thenReturn(confirmed);

            var response = matchService.confirmMatch(matchId, opponentA, "idem-red-ac4-referee");

            assertThat(response.status()).isEqualTo(Match.STATUS_CONFIRMED);
            assertThat(response.cooldownExpiresAt()).isNull();
        }
    }

    @Nested
    @DisplayName("AC5: Double confirmation → idempotent, no state change")
    class DoubleConfirmationIdempotency {

        @Test
        @DisplayName("[P0] RED: Should return current state without error when same opponent confirms twice")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_doubleConfirm_returnsCurrentStateWithoutError() {
            Match confirmed = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamADefenderId(UUID.randomUUID())
                    .teamBAttackerId(opponentA)
                    .teamBDefenderId(opponentB)
                    .status(Match.STATUS_CONFIRMED)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .confirmedByUserId(opponentA)
                    .confirmedAt(Instant.now())
                    .cooldownExpiresAt(null)
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(confirmed));
            when(matchOperation.confirmMatch(any(Match.class), eq(opponentA))).thenReturn(confirmed);

            var response = matchService.confirmMatch(matchId, opponentA, "idem-red-ac5");

            assertThat(response.status()).isEqualTo(Match.STATUS_CONFIRMED);
            assertThat(response.confirmedAt()).isNotNull();
        }
    }
}
