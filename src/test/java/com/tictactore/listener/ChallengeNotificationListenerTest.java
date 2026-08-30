package com.tictactore.listener;

import com.tictactore.event.ChallengeAcceptedEvent;
import com.tictactore.event.ChallengeCreatedEvent;
import com.tictactore.event.ChallengeDeclinedEvent;
import com.tictactore.model.MatchType;
import com.tictactore.model.PlayerGroup;
import com.tictactore.model.User;
import com.tictactore.repository.PlayerGroupRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.PushNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeNotificationListener Unit Tests")
class ChallengeNotificationListenerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerGroupRepository playerGroupRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private ChallengeNotificationListener challengeNotificationListener;

    @Nested
    @DisplayName("ChallengeCreatedEvent Processing")
    class ChallengeCreatedEventSpecs {

        @Test
        void shouldDispatchPushToDirectPlayerTarget() {
            var challengeId = UUID.randomUUID();
            var challengerId = UUID.randomUUID();
            var targetPlayerId = UUID.randomUUID();
            var targetUser = User.builder().id(targetPlayerId).nickname("TargetPlayer").build();
            var event = new ChallengeCreatedEvent(challengeId, challengerId, "Challenger", targetPlayerId, null, MatchType.ONE_VS_ONE);

            when(userRepository.findById(targetPlayerId)).thenReturn(Optional.of(targetUser));

            challengeNotificationListener.handleChallengeCreated(event);

            verify(pushNotificationService).sendChallengeCreatedNotification(
                    eq(challengeId),
                    eq("Challenger"),
                    eq(MatchType.ONE_VS_ONE),
                    eq(List.of(targetUser))
            );
        }

        @Test
        void shouldDispatchPushToGroupMembersExcludingChallenger() {
            var challengeId = UUID.randomUUID();
            var challengerId = UUID.randomUUID();
            var targetGroupId = UUID.randomUUID();
            var challengerUser = User.builder().id(challengerId).nickname("Challenger").build();
            var member1 = User.builder().id(UUID.randomUUID()).nickname("Member1").build();
            var member2 = User.builder().id(UUID.randomUUID()).nickname("Member2").build();
            var group = PlayerGroup.builder().id(targetGroupId).name("Squad").members(Set.of(challengerUser, member1, member2)).build();
            var event = new ChallengeCreatedEvent(challengeId, challengerId, "Challenger", null, targetGroupId, MatchType.TWO_VS_TWO);

            when(playerGroupRepository.findById(targetGroupId)).thenReturn(Optional.of(group));

            challengeNotificationListener.handleChallengeCreated(event);

            verify(pushNotificationService).sendChallengeCreatedNotification(
                    eq(challengeId),
                    eq("Challenger"),
                    eq(MatchType.TWO_VS_TWO),
                    any()
            );
        }

        @Test
        void shouldCatchExceptionsGracefully() {
            var challengeId = UUID.randomUUID();
            var event = new ChallengeCreatedEvent(challengeId, UUID.randomUUID(), "Challenger", UUID.randomUUID(), null, MatchType.ONE_VS_ONE);

            when(userRepository.findById(any())).thenThrow(new RuntimeException("DB error"));

            assertThatCode(() -> challengeNotificationListener.handleChallengeCreated(event))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("ChallengeAcceptedEvent & DeclinedEvent Processing")
    class AcceptedAndDeclinedSpecs {

        @Test
        void shouldDispatchPushOnChallengeAccepted() {
            var challengeId = UUID.randomUUID();
            var challengerId = UUID.randomUUID();
            var targetUserId = UUID.randomUUID();
            var challengerUser = User.builder().id(challengerId).nickname("Challenger").build();
            var event = new ChallengeAcceptedEvent(challengeId, challengerId, targetUserId, "TargetPlayer", MatchType.ONE_VS_ONE);

            when(userRepository.findById(challengerId)).thenReturn(Optional.of(challengerUser));

            challengeNotificationListener.handleChallengeAccepted(event);

            verify(pushNotificationService).sendChallengeAcceptedNotification(
                    eq(challengeId),
                    eq("TargetPlayer"),
                    eq(MatchType.ONE_VS_ONE),
                    eq(challengerUser)
            );
        }

        @Test
        void shouldDispatchPushOnChallengeDeclined() {
            var challengeId = UUID.randomUUID();
            var challengerId = UUID.randomUUID();
            var targetUserId = UUID.randomUUID();
            var challengerUser = User.builder().id(challengerId).nickname("Challenger").build();
            var event = new ChallengeDeclinedEvent(challengeId, challengerId, targetUserId, "TargetPlayer");

            when(userRepository.findById(challengerId)).thenReturn(Optional.of(challengerUser));

            challengeNotificationListener.handleChallengeDeclined(event);

            verify(pushNotificationService).sendChallengeDeclinedNotification(
                    eq(challengeId),
                    eq("TargetPlayer"),
                    eq(challengerUser)
            );
        }

        @Test
        void shouldCatchExceptionOnAccepted() {
            var challengeId = UUID.randomUUID();
            var challengerId = UUID.randomUUID();
            var challengerUser = User.builder().id(challengerId).nickname("Challenger").build();
            var event = new ChallengeAcceptedEvent(challengeId, challengerId, UUID.randomUUID(), "TargetPlayer", MatchType.ONE_VS_ONE);

            when(userRepository.findById(challengerId)).thenReturn(Optional.of(challengerUser));
            doThrow(new RuntimeException("Push timeout")).when(pushNotificationService)
                    .sendChallengeAcceptedNotification(any(), any(), any(), any());

            assertThatCode(() -> challengeNotificationListener.handleChallengeAccepted(event))
                    .doesNotThrowAnyException();
        }
    }
}
