package com.tictactore.service;

import com.tictactore.dto.ChallengeActionResponse;
import com.tictactore.dto.ChallengeResponse;
import com.tictactore.dto.CreateChallengeRequest;
import com.tictactore.event.ChallengeAcceptedEvent;
import com.tictactore.event.ChallengeCreatedEvent;
import com.tictactore.event.ChallengeDeclinedEvent;
import com.tictactore.exception.ChallengeConflictException;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.exception.ValidationException;
import com.tictactore.model.ChallengeStatus;
import com.tictactore.model.MatchChallenge;
import com.tictactore.model.MatchType;
import com.tictactore.model.PlayerGroup;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.User;
import com.tictactore.repository.MatchChallengeRepository;
import com.tictactore.repository.PlayerGroupRepository;
import com.tictactore.repository.RuleConfigurationRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.impl.ChallengeServiceImpl;
import com.tictactore.service.operation.ChallengeOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeServiceImpl Unit Tests")
class ChallengeServiceTest {

    @Mock
    private MatchChallengeRepository matchChallengeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerGroupRepository playerGroupRepository;

    @Mock
    private RuleConfigurationRepository ruleConfigurationRepository;

    @Mock
    private ChallengeOperation challengeOperation;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ChallengeServiceImpl challengeService;

    private UUID challengerId;
    private User challengerUser;
    private UUID targetPlayerId;
    private User targetPlayerUser;
    private UUID targetGroupId;
    private PlayerGroup targetGroup;

    @BeforeEach
    void setUp() {
        challengerId = UUID.randomUUID();
        challengerUser = User.builder().id(challengerId).nickname("Challenger").email("challenger@example.com").build();

        targetPlayerId = UUID.randomUUID();
        targetPlayerUser = User.builder().id(targetPlayerId).nickname("Target").email("target@example.com").build();

        targetGroupId = UUID.randomUUID();
        targetGroup = PlayerGroup.builder().id(targetGroupId).name("Alpha Squad").creatorId(targetPlayerId).members(Set.of(targetPlayerUser)).build();
    }

    @Nested
    @DisplayName("createChallenge Tests")
    class CreateChallengeSpecs {

        @Test
        void shouldCreatePlayerChallengeSuccessfully() {
            var request = new CreateChallengeRequest(targetPlayerId, null, MatchType.ONE_VS_ONE, null, "Game on!");
            when(userRepository.findById(challengerId)).thenReturn(Optional.of(challengerUser));
            when(userRepository.findById(targetPlayerId)).thenReturn(Optional.of(targetPlayerUser));
            when(matchChallengeRepository.existsPendingBetweenPlayers(challengerId, targetPlayerId, ChallengeStatus.PENDING)).thenReturn(false);

            var saved = MatchChallenge.builder()
                    .id(UUID.randomUUID())
                    .challenger(challengerUser)
                    .targetPlayer(targetPlayerUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .message("Game on!")
                    .status(ChallengeStatus.PENDING)
                    .createdAt(Instant.now())
                    .build();
            when(challengeOperation.saveChallenge(any(MatchChallenge.class))).thenReturn(saved);

            var response = challengeService.createChallenge(challengerId, request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(saved.getId());
            assertThat(response.challengerId()).isEqualTo(challengerId);
            assertThat(response.targetPlayerId()).isEqualTo(targetPlayerId);
            assertThat(response.status()).isEqualTo(ChallengeStatus.PENDING);
            verify(eventPublisher).publishEvent(any(ChallengeCreatedEvent.class));
        }

        @Test
        void shouldCreateGroupChallengeWithRuleConfigSuccessfully() {
            var ruleConfigId = UUID.randomUUID();
            var ruleConfig = RuleConfiguration.builder().id(ruleConfigId).name("Pro Rules").build();
            var request = new CreateChallengeRequest(null, targetGroupId, MatchType.TWO_VS_TWO, ruleConfigId, "Team match!");

            when(userRepository.findById(challengerId)).thenReturn(Optional.of(challengerUser));
            when(playerGroupRepository.findById(targetGroupId)).thenReturn(Optional.of(targetGroup));
            when(matchChallengeRepository.existsByChallengerIdAndTargetGroupIdAndStatus(challengerId, targetGroupId, ChallengeStatus.PENDING)).thenReturn(false);
            when(ruleConfigurationRepository.findById(ruleConfigId)).thenReturn(Optional.of(ruleConfig));

            var saved = MatchChallenge.builder()
                    .id(UUID.randomUUID())
                    .challenger(challengerUser)
                    .targetGroup(targetGroup)
                    .matchType(MatchType.TWO_VS_TWO)
                    .ruleConfig(ruleConfig)
                    .message("Team match!")
                    .status(ChallengeStatus.PENDING)
                    .createdAt(Instant.now())
                    .build();
            when(challengeOperation.saveChallenge(any(MatchChallenge.class))).thenReturn(saved);

            var response = challengeService.createChallenge(challengerId, request);

            assertThat(response).isNotNull();
            assertThat(response.targetGroupId()).isEqualTo(targetGroupId);
            assertThat(response.ruleConfigId()).isEqualTo(ruleConfigId);
            assertThat(response.ruleConfigName()).isEqualTo("Pro Rules");
            verify(eventPublisher).publishEvent(any(ChallengeCreatedEvent.class));
        }

        @Test
        void shouldThrowExceptionWhenNoTargetSpecified() {
            var request = new CreateChallengeRequest(null, null, MatchType.ONE_VS_ONE, null, null);

            assertThatThrownBy(() -> challengeService.createChallenge(challengerId, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Challenge must target either a player or a group, not both");
        }

        @Test
        void shouldThrowExceptionWhenBothPlayerAndGroupTargetsSpecified() {
            var request = new CreateChallengeRequest(targetPlayerId, targetGroupId, MatchType.ONE_VS_ONE, null, null);

            assertThatThrownBy(() -> challengeService.createChallenge(challengerId, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Challenge must target either a player or a group, not both");
        }

        @Test
        void shouldThrowExceptionWhenSelfChallenging() {
            var request = new CreateChallengeRequest(challengerId, null, MatchType.ONE_VS_ONE, null, null);

            assertThatThrownBy(() -> challengeService.createChallenge(challengerId, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Challenger cannot challenge themselves");
        }

        @Test
        void shouldThrowConflictWhenPendingPlayerChallengeAlreadyExists() {
            var request = new CreateChallengeRequest(targetPlayerId, null, MatchType.ONE_VS_ONE, null, null);
            when(userRepository.findById(challengerId)).thenReturn(Optional.of(challengerUser));
            when(userRepository.findById(targetPlayerId)).thenReturn(Optional.of(targetPlayerUser));
            when(matchChallengeRepository.existsPendingBetweenPlayers(challengerId, targetPlayerId, ChallengeStatus.PENDING)).thenReturn(true);

            assertThatThrownBy(() -> challengeService.createChallenge(challengerId, request))
                    .isInstanceOf(ChallengeConflictException.class)
                    .hasMessageContaining("An active pending challenge already exists for this player");
        }

        @Test
        void shouldThrowConflictWhenPendingGroupChallengeAlreadyExists() {
            var request = new CreateChallengeRequest(null, targetGroupId, MatchType.TWO_VS_TWO, null, null);
            when(userRepository.findById(challengerId)).thenReturn(Optional.of(challengerUser));
            when(playerGroupRepository.findById(targetGroupId)).thenReturn(Optional.of(targetGroup));
            when(matchChallengeRepository.existsByChallengerIdAndTargetGroupIdAndStatus(challengerId, targetGroupId, ChallengeStatus.PENDING)).thenReturn(true);

            assertThatThrownBy(() -> challengeService.createChallenge(challengerId, request))
                    .isInstanceOf(ChallengeConflictException.class)
                    .hasMessageContaining("An active pending challenge already exists for this group");
        }
    }

    @Nested
    @DisplayName("getIncomingChallenges & getOutgoingChallenges Tests")
    class QuerySpecs {

        @Test
        void shouldReturnIncomingChallengesExcludingOwn() {
            var challenge = MatchChallenge.builder()
                    .id(UUID.randomUUID())
                    .challenger(challengerUser)
                    .targetPlayer(targetPlayerUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .status(ChallengeStatus.PENDING)
                    .build();

            when(playerGroupRepository.findGroupIdsByMemberId(targetPlayerId)).thenReturn(List.of(targetGroupId));
            when(matchChallengeRepository.findIncomingChallenges(eq(targetPlayerId), eq(List.of(targetGroupId)), eq(ChallengeStatus.PENDING)))
                    .thenReturn(List.of(challenge));

            var incoming = challengeService.getIncomingChallenges(targetPlayerId);

            assertThat(incoming).hasSize(1);
            assertThat(incoming.get(0).id()).isEqualTo(challenge.getId());
        }

        @Test
        void shouldReturnOutgoingChallenges() {
            var challenge = MatchChallenge.builder()
                    .id(UUID.randomUUID())
                    .challenger(challengerUser)
                    .targetPlayer(targetPlayerUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .status(ChallengeStatus.PENDING)
                    .build();

            when(matchChallengeRepository.findByChallengerIdAndStatus(challengerId, ChallengeStatus.PENDING))
                    .thenReturn(List.of(challenge));

            var outgoing = challengeService.getOutgoingChallenges(challengerId);

            assertThat(outgoing).hasSize(1);
            assertThat(outgoing.get(0).id()).isEqualTo(challenge.getId());
        }
    }

    @Nested
    @DisplayName("getChallengeById Tests")
    class GetByIdSpecs {

        @Test
        void shouldReturnChallengeWhenAuthorized() {
            var challengeId = UUID.randomUUID();
            var challenge = MatchChallenge.builder()
                    .id(challengeId)
                    .challenger(challengerUser)
                    .targetPlayer(targetPlayerUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .status(ChallengeStatus.PENDING)
                    .build();

            when(matchChallengeRepository.findByIdWithDetails(challengeId)).thenReturn(Optional.of(challenge));
            when(playerGroupRepository.findGroupIdsByMemberId(targetPlayerId)).thenReturn(List.of());

            var response = challengeService.getChallengeById(challengeId, targetPlayerId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(challengeId);
        }

        @Test
        void shouldThrowAccessDeniedWhenNotAuthorized() {
            var challengeId = UUID.randomUUID();
            var unauthorizedUserId = UUID.randomUUID();
            var challenge = MatchChallenge.builder()
                    .id(challengeId)
                    .challenger(challengerUser)
                    .targetPlayer(targetPlayerUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .status(ChallengeStatus.PENDING)
                    .build();

            when(matchChallengeRepository.findByIdWithDetails(challengeId)).thenReturn(Optional.of(challenge));
            when(playerGroupRepository.findGroupIdsByMemberId(unauthorizedUserId)).thenReturn(List.of());

            assertThatThrownBy(() -> challengeService.getChallengeById(challengeId, unauthorizedUserId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("accept, decline & cancel Tests")
    class ActionSpecs {

        @Test
        void shouldAcceptChallengeAndPublishEvent() {
            var challengeId = UUID.randomUUID();
            var challenge = MatchChallenge.builder()
                    .id(challengeId)
                    .challenger(challengerUser)
                    .targetPlayer(targetPlayerUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .status(ChallengeStatus.ACCEPTED)
                    .build();

            when(playerGroupRepository.findGroupIdsByMemberId(targetPlayerId)).thenReturn(List.of());
            when(challengeOperation.acceptChallenge(eq(challengeId), eq(targetPlayerId), any())).thenReturn(challenge);
            when(userRepository.findById(targetPlayerId)).thenReturn(Optional.of(targetPlayerUser));

            var response = challengeService.acceptChallenge(challengeId, targetPlayerId);

            assertThat(response.challengeId()).isEqualTo(challengeId);
            assertThat(response.status()).isEqualTo(ChallengeStatus.ACCEPTED);
            verify(eventPublisher).publishEvent(any(ChallengeAcceptedEvent.class));
        }

        @Test
        void shouldDeclineChallengeAndPublishEvent() {
            var challengeId = UUID.randomUUID();
            var challenge = MatchChallenge.builder()
                    .id(challengeId)
                    .challenger(challengerUser)
                    .targetPlayer(targetPlayerUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .status(ChallengeStatus.DECLINED)
                    .build();

            when(playerGroupRepository.findGroupIdsByMemberId(targetPlayerId)).thenReturn(List.of());
            when(challengeOperation.declineChallenge(eq(challengeId), eq(targetPlayerId), any())).thenReturn(challenge);
            when(userRepository.findById(targetPlayerId)).thenReturn(Optional.of(targetPlayerUser));

            var response = challengeService.declineChallenge(challengeId, targetPlayerId);

            assertThat(response.challengeId()).isEqualTo(challengeId);
            assertThat(response.status()).isEqualTo(ChallengeStatus.DECLINED);
            verify(eventPublisher).publishEvent(any(ChallengeDeclinedEvent.class));
        }

        @Test
        void shouldCancelChallenge() {
            var challengeId = UUID.randomUUID();
            var challenge = MatchChallenge.builder()
                    .id(challengeId)
                    .challenger(challengerUser)
                    .targetPlayer(targetPlayerUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .status(ChallengeStatus.CANCELLED)
                    .build();

            when(challengeOperation.cancelChallenge(eq(challengeId), eq(challengerId))).thenReturn(challenge);

            var response = challengeService.cancelChallenge(challengeId, challengerId);

            assertThat(response.challengeId()).isEqualTo(challengeId);
            assertThat(response.status()).isEqualTo(ChallengeStatus.CANCELLED);
        }
    }
}
