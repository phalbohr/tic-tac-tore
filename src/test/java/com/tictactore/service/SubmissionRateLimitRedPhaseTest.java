package com.tictactore.service;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.GameDto;
import com.tictactore.exception.RateLimitExceededException;
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
 * Red-phase acceptance test scaffolds for Story 3.6: Submission Rate Limiting (Anti-Spam).
 * <p>
 * These tests are emitted in the TDD RED PHASE. They assert the expected behavior
 * defined by the acceptance criteria (AC1–AC6) and would fail if the implementation
 * were absent. In the current green-phase state they are disabled to avoid duplicate
 * coverage with the active specs in {@link RateLimitServiceTest} and
 * {@link MatchServiceTest}.
 * <p>
 * To activate: remove {@code @Disabled} and run.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubmissionRateLimit — Red-Phase Scaffolds")
class SubmissionRateLimitRedPhaseTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchOperation matchOperation;

    @Mock
    private com.tictactore.service.PushNotificationService pushNotificationService;

    @Mock
    private com.tictactore.service.RateLimitService rateLimitService;

    @InjectMocks
    private MatchServiceImpl matchService;

    private UUID creatorId;
    private UUID opponentA;
    private UUID opponentB;

    @BeforeEach
    void setUp() {
        creatorId = UUID.randomUUID();
        opponentA = UUID.randomUUID();
        opponentB = UUID.randomUUID();
    }

    @Nested
    @DisplayName("AC1: Happy path — under limits")
    class HappyPath {

        @Test
        @DisplayName("[P0] RED: Should create match when user is under submission and rejection limits")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_createMatch_succeeds_whenUnderLimits() {
            CreateMatchRequest request = new CreateMatchRequest(
                    "red-ac1",
                    creatorId, creatorId, opponentA, opponentB, null,
                    List.of(new GameDto(10, 8, creatorId, opponentA, opponentB, null)),
                    null, null
            );

            when(matchRepository.findByIdempotencyKey("red-ac1")).thenReturn(Optional.empty());
            doNothing().when(rateLimitService).checkSubmissionLimit(creatorId);
            when(matchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Match created = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamBAttackerId(opponentA)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .createdAt(Instant.now())
                    .build();

            when(matchOperation.saveMatch(any())).thenReturn(created);

            var response = matchService.createMatch(request);

            assertThat(response.id()).isEqualTo(created.getId());
            assertThat(response.status()).isEqualTo(Match.STATUS_PENDING_APPROVAL);
            verify(rateLimitService).checkSubmissionLimit(creatorId);
        }
    }

    @Nested
    @DisplayName("AC2: Hourly submission limit exceeded")
    class HourlyLimitExceeded {

        @Test
        @DisplayName("[P0] RED: Should throw RateLimitExceededException when submission limit exceeded")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_createMatch_throws_whenSubmissionLimitExceeded() {
            CreateMatchRequest request = new CreateMatchRequest(
                    "red-ac2",
                    creatorId, creatorId, opponentA, opponentB, null,
                    List.of(new GameDto(10, 8, creatorId, opponentA, opponentB, null)),
                    null, null
            );

            when(matchRepository.findByIdempotencyKey("red-ac2")).thenReturn(Optional.empty());
            doThrow(new RateLimitExceededException(3600, "Rate limit exceeded: too many match submissions"))
                    .when(rateLimitService).checkSubmissionLimit(creatorId);

            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("too many match submissions");

            verifyNoInteractions(matchOperation);
        }
    }

    @Nested
    @DisplayName("AC3: Rejection throttle")
    class RejectionThrottle {

        @Test
        @DisplayName("[P0] RED: Should throw RateLimitExceededException when rejection threshold exceeded")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_createMatch_throws_whenRejectionThresholdExceeded() {
            CreateMatchRequest request = new CreateMatchRequest(
                    "red-ac3",
                    creatorId, creatorId, opponentA, opponentB, null,
                    List.of(new GameDto(10, 8, creatorId, opponentA, opponentB, null)),
                    null, null
            );

            when(matchRepository.findByIdempotencyKey("red-ac3")).thenReturn(Optional.empty());
            doThrow(new RateLimitExceededException(7200, "Rate limit exceeded: too many rejected matches"))
                    .when(rateLimitService).checkSubmissionLimit(creatorId);

            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("too many rejected matches");

            verifyNoInteractions(matchOperation);
        }
    }

    @Nested
    @DisplayName("AC5: Idempotent retry")
    class IdempotentRetry {

        @Test
        @DisplayName("[P0] RED: Should return existing match without incrementing submission counter on idempotent resubmission")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_createMatch_returnsExistingMatch_withoutIncrementingCounter_onIdempotentResubmission() {
            var existingMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamBAttackerId(opponentA)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .createdAt(Instant.now())
                    .games(List.of())
                    .build();

            when(matchRepository.findByIdempotencyKey("red-ac5")).thenReturn(Optional.of(existingMatch));
            when(userRepository.findAllById(any())).thenReturn(List.of());

            var response = matchService.createMatch(new CreateMatchRequest(
                    "red-ac5",
                    creatorId, creatorId, opponentA, opponentB, null,
                    List.of(new GameDto(10, 8, creatorId, opponentA, opponentB, null)),
                    null, null
            ));

            assertThat(response.id()).isEqualTo(existingMatch.getId());
            verify(rateLimitService, never()).checkSubmissionLimit(any());
        }
    }

    @Nested
    @DisplayName("AC3 + Integration: Rejection recording")
    class RejectionRecording {

        @Test
        @DisplayName("[P0] RED: rejectMatch should record rejection via rateLimitService")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_rejectMatch_recordsRejection() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamBAttackerId(opponentA)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .createdAt(Instant.now())
                    .build();

            var rejectedMatch = Match.builder()
                    .id(matchId)
                    .creatorId(creatorId)
                    .teamAAttackerId(creatorId)
                    .teamBAttackerId(opponentA)
                    .status(Match.STATUS_REJECTED)
                    .rejectedByUserId(opponentA)
                    .rejectedAt(Instant.now())
                    .rejectionReason("Wrong score")
                    .createdAt(Instant.now())
                    .build();

            var creatorUser = User.builder().id(creatorId).nickname("CreatorPlayer").build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchOperation.rejectMatch(eq(matchId), eq(opponentA), any(), any())).thenReturn(rejectedMatch);
            when(userRepository.findById(creatorId)).thenReturn(Optional.of(creatorUser));

            var request = new com.tictactore.dto.MatchRejectionRequest("Wrong score", null);
            matchService.rejectMatch(matchId, opponentA, request, "idem-red-reject");

            verify(rateLimitService).recordRejection(opponentA);
            verify(matchOperation).rejectMatch(matchId, opponentA, "Wrong score", null);
        }
    }

    @Nested
    @DisplayName("AC6: Redis failure fail-closed")
    class RedisFailure {

        @Test
        @DisplayName("[P0] RED: Should throw RateLimitExceededException with redisFailure=true when Redis is unavailable")
        @org.junit.jupiter.api.Disabled("Red-phase scaffold — remove @Disabled to activate")
        void red_checkSubmissionLimit_throwsRedisFailure_whenRedisUnavailable() {
            doThrow(new RateLimitExceededException("Redis unavailable", new RuntimeException("Redis down")))
                    .when(rateLimitService).checkSubmissionLimit(creatorId);

            CreateMatchRequest request = new CreateMatchRequest(
                    "red-ac6",
                    creatorId, creatorId, opponentA, opponentB, null,
                    List.of(new GameDto(10, 8, creatorId, opponentA, opponentB, null)),
                    null, null
            );

            when(matchRepository.findByIdempotencyKey("red-ac6")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(RateLimitExceededException.class);
        }
    }
}
