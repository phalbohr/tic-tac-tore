package com.tictactore.service;

import com.tictactore.dto.CreateChallengeRequest;
import com.tictactore.event.ChallengeAcceptedEvent;
import com.tictactore.event.ChallengeCreatedEvent;
import com.tictactore.event.ChallengeDeclinedEvent;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.ChallengeStatus;
import com.tictactore.model.MatchChallenge;
import com.tictactore.model.MatchType;
import com.tictactore.model.PlayerGroup;
import com.tictactore.model.PlayerGroupMember;
import com.tictactore.model.User;
import com.tictactore.repository.MatchChallengeRepository;
import com.tictactore.repository.PlayerGroupMemberRepository;
import com.tictactore.repository.PlayerGroupRepository;
import com.tictactore.repository.RuleConfigurationRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.impl.ChallengeServiceImpl;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeService ATDD Specifications — Business Rules & State Transitions (Story 6.6)")
class ChallengeServiceATDDTest {

    @Mock
    private MatchChallengeRepository matchChallengeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerGroupRepository playerGroupRepository;

    @Mock
    private PlayerGroupMemberRepository playerGroupMemberRepository;

    @Mock
    private RuleConfigurationRepository ruleConfigurationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ChallengeServiceImpl challengeService;

    private UUID challengerId;
    private UUID targetPlayerId;
    private User challenger;
    private User targetPlayer;

    @BeforeEach
    void setUp() {
        challengerId = UUID.randomUUID();
        targetPlayerId = UUID.randomUUID();

        challenger = User.builder()
                .id(challengerId)
                .nickname("ChallengerPro")
                .email("challenger@example.com")
                .avatarUrl("https://example.com/c.png")
                .build();

        targetPlayer = User.builder()
                .id(targetPlayerId)
                .nickname("TargetNick")
                .email("target@example.com")
                .avatarUrl("https://example.com/t.png")
                .build();
    }

    @Nested
    @DisplayName("Challenge Creation Logic (AC1)")
    class CreateChallengeLogicTests {

        @Test
        @DisplayName("Should create challenge, persist PENDING record, and publish ChallengeCreatedEvent")
        void shouldCreateChallengeAndPublishEvent() {
            var request = new CreateChallengeRequest(targetPlayerId, null, MatchType.ONE_VS_ONE, null, "Game on!");
            when(userRepository.findById(challengerId)).thenReturn(Optional.of(challenger));
            when(userRepository.findById(targetPlayerId)).thenReturn(Optional.of(targetPlayer));
            when(matchChallengeRepository.existsByChallengerIdAndTargetPlayerIdAndStatus(challengerId, targetPlayerId, ChallengeStatus.PENDING))
                    .thenReturn(false);
            when(matchChallengeRepository.save(any(MatchChallenge.class)))
                    .thenAnswer(invocation -> {
                        MatchChallenge entity = invocation.getArgument(0);
                        entity.setId(UUID.randomUUID());
                        entity.setCreatedAt(Instant.now());
                        return entity;
                    });

            var response = challengeService.createChallenge(challengerId, request);

            assertThat(response).isNotNull();
            assertThat(response.challengerId()).isEqualTo(challengerId);
            assertThat(response.targetPlayerId()).isEqualTo(targetPlayerId);
            assertThat(response.status()).isEqualTo(ChallengeStatus.PENDING);
            assertThat(response.message()).isEqualTo("Game on!");

            var eventCaptor = ArgumentCaptor.forClass(ChallengeCreatedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().challengerId()).isEqualTo(challengerId);
            assertThat(eventCaptor.getValue().targetPlayerId()).isEqualTo(targetPlayerId);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user challenges themselves")
        void shouldThrowException_whenSelfChallenging() {
            var request = new CreateChallengeRequest(challengerId, null, MatchType.ONE_VS_ONE, null, "Self");

            assertThatThrownBy(() -> challengeService.createChallenge(challengerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Challenger cannot challenge themselves");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when active pending challenge exists")
        void shouldThrowException_whenDuplicatePendingExists() {
            var request = new CreateChallengeRequest(targetPlayerId, null, MatchType.ONE_VS_ONE, null, "Again");
            when(userRepository.findById(challengerId)).thenReturn(Optional.of(challenger));
            when(userRepository.findById(targetPlayerId)).thenReturn(Optional.of(targetPlayer));
            when(matchChallengeRepository.existsByChallengerIdAndTargetPlayerIdAndStatus(challengerId, targetPlayerId, ChallengeStatus.PENDING))
                    .thenReturn(true);

            assertThatThrownBy(() -> challengeService.createChallenge(challengerId, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("An active pending challenge already exists");
        }
    }

    @Nested
    @DisplayName("Challenge Acceptance Logic (AC3)")
    class AcceptChallengeLogicTests {

        @Test
        @DisplayName("Should accept challenge, transition to ACCEPTED, and publish ChallengeAcceptedEvent")
        void shouldAcceptChallengeSuccessfully() {
            var challengeId = UUID.randomUUID();
            var challenge = MatchChallenge.builder()
                    .id(challengeId)
                    .challenger(challenger)
                    .targetPlayer(targetPlayer)
                    .matchType(MatchType.ONE_VS_ONE)
                    .status(ChallengeStatus.PENDING)
                    .build();

            when(matchChallengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
            when(userRepository.findById(targetPlayerId)).thenReturn(Optional.of(targetPlayer));
            when(matchChallengeRepository.save(any(MatchChallenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var result = challengeService.acceptChallenge(challengeId, targetPlayerId);

            assertThat(result.status()).isEqualTo(ChallengeStatus.ACCEPTED);
            assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.ACCEPTED);

            var eventCaptor = ArgumentCaptor.forClass(ChallengeAcceptedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().challengeId()).isEqualTo(challengeId);
            assertThat(eventCaptor.getValue().challengerId()).isEqualTo(challengerId);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when non-target user tries to accept")
        void shouldThrowAccessDenied_whenUserNotAuthorized() {
            var challengeId = UUID.randomUUID();
            var unauthorizedUserId = UUID.randomUUID();
            var challenge = MatchChallenge.builder()
                    .id(challengeId)
                    .challenger(challenger)
                    .targetPlayer(targetPlayer)
                    .status(ChallengeStatus.PENDING)
                    .build();

            when(matchChallengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));

            assertThatThrownBy(() -> challengeService.acceptChallenge(challengeId, unauthorizedUserId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("Challenge Decline & Cancel Logic (AC4)")
    class DeclineAndCancelLogicTests {

        @Test
        @DisplayName("Should decline challenge and publish ChallengeDeclinedEvent")
        void shouldDeclineChallengeSuccessfully() {
            var challengeId = UUID.randomUUID();
            var challenge = MatchChallenge.builder()
                    .id(challengeId)
                    .challenger(challenger)
                    .targetPlayer(targetPlayer)
                    .matchType(MatchType.ONE_VS_ONE)
                    .status(ChallengeStatus.PENDING)
                    .build();

            when(matchChallengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
            when(userRepository.findById(targetPlayerId)).thenReturn(Optional.of(targetPlayer));
            when(matchChallengeRepository.save(any(MatchChallenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var result = challengeService.declineChallenge(challengeId, targetPlayerId);

            assertThat(result.status()).isEqualTo(ChallengeStatus.DECLINED);
            assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.DECLINED);

            var eventCaptor = ArgumentCaptor.forClass(ChallengeDeclinedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().challengeId()).isEqualTo(challengeId);
        }

        @Test
        @DisplayName("Should cancel challenge by challenger")
        void shouldCancelChallengeByChallenger() {
            var challengeId = UUID.randomUUID();
            var challenge = MatchChallenge.builder()
                    .id(challengeId)
                    .challenger(challenger)
                    .targetPlayer(targetPlayer)
                    .status(ChallengeStatus.PENDING)
                    .build();

            when(matchChallengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
            when(matchChallengeRepository.save(any(MatchChallenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var result = challengeService.cancelChallenge(challengeId, challengerId);

            assertThat(result.status()).isEqualTo(ChallengeStatus.CANCELLED);
            assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.CANCELLED);
        }
    }
}
