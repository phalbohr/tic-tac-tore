package com.tictactore.service;

import com.tictactore.dto.CreateMatchRequest;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MatchService Rate Limit Tests")
class MatchServiceRateLimitTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchOperation matchOperation;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private MatchServiceImpl matchService;

    private UUID p1, p2;

    @BeforeEach
    void setUp() {
        p1 = UUID.randomUUID();
        p2 = UUID.randomUUID();

        when(userRepository.findAllById(any())).thenReturn(List.of(
                user(p1, "Player1"),
                user(p2, "Player2")));
    }

    @Nested
    @DisplayName("createMatch Rate Limit Specs")
    class CreateMatchRateLimitSpecs {

        @Test
        @DisplayName("[P0] Should throw RateLimitExceededException when submission limit is exceeded")
        void shouldThrowRateLimitExceeded_whenSubmissionLimitExceeded() {
            var request = new CreateMatchRequest(
                    null, p1, p1, null, p2, null,
                    List.of(new com.tictactore.dto.GameDto(10, 5, null, null, null, null)),
                    null, null);

            when(matchRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            doThrow(new RateLimitExceededException(30, "Rate limit exceeded"))
                    .when(rateLimitService).checkSubmissionLimit(any());

            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(RateLimitExceededException.class);
        }

        @Test
        @DisplayName("[P1] Should call rateLimitService.checkSubmissionLimit after idempotency check passes")
        void shouldCallCheckSubmissionLimit_afterIdempotencyCheck() {
            var request = new CreateMatchRequest(
                    null, p1, p1, null, p2, null,
                    List.of(new com.tictactore.dto.GameDto(10, 5, null, null, null, null)),
                    null, null);

            when(matchRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(matchOperation.saveMatch(any())).thenAnswer(i -> i.getArgument(0));

            matchService.createMatch(request);

            verify(rateLimitService).checkSubmissionLimit(p1);
        }

        @Test
        @DisplayName("[P1] Should not increment rate-limit counter when idempotency key matches existing match")
        void shouldNotIncrementCounter_whenIdempotentResubmission() {
            var request = new CreateMatchRequest(
                    "idem-key", p1, p1, null, p2, null,
                    List.of(new com.tictactore.dto.GameDto(10, 5, null, null, null, null)),
                    null, null);
            var existing = Match.builder()
                    .idempotencyKey("idem-key")
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("PENDING_APPROVAL")
                    .createdAt(java.time.Instant.now())
                    .build();

            when(matchRepository.findByIdempotencyKey("idem-key")).thenReturn(Optional.of(existing));

            matchService.createMatch(request);

            verify(rateLimitService, never()).checkSubmissionLimit(any());
        }
    }

    @Nested
    @DisplayName("rejectMatch Rate Limit Specs")
    class RejectMatchRateLimitSpecs {

        @Test
        @DisplayName("[P1] Should call rateLimitService.recordRejection when match is rejected")
        void shouldRecordRejection_whenMatchRejected() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("PENDING_APPROVAL")
                    .createdAt(java.time.Instant.now())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchOperation.rejectMatch(any(), any(), any(), any())).thenAnswer(i -> {
                UUID id = i.getArgument(0);
                return Match.builder()
                        .id(id)
                        .status("REJECTED")
                        .rejectedByUserId(i.getArgument(1))
                        .rejectedAt(java.time.Instant.now())
                        .build();
            });

            matchService.rejectMatch(matchId, p1, null, null);

            verify(rateLimitService).recordRejection(p1);
        }
    }

    private static User user(UUID id, String nickname) {
        var u = new User();
        u.setId(id);
        u.setNickname(nickname);
        u.setEmail(nickname + "@example.com");
        return u;
    }
}
