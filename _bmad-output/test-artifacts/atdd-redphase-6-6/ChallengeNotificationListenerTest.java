package com.tictactore.listener;

import com.tictactore.event.ChallengeAcceptedEvent;
import com.tictactore.event.ChallengeCreatedEvent;
import com.tictactore.event.ChallengeDeclinedEvent;
import com.tictactore.model.MatchType;
import com.tictactore.model.PlayerGroup;
import com.tictactore.model.PlayerGroupMember;
import com.tictactore.model.User;
import com.tictactore.repository.PlayerGroupRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.PushNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeNotificationListener ATDD Specifications — Event Push Dispatch (Story 6.6)")
class ChallengeNotificationListenerTest {

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerGroupRepository playerGroupRepository;

    @InjectMocks
    private ChallengeNotificationListener challengeNotificationListener;

    private UUID challengerId;
    private UUID targetPlayerId;
    private UUID targetGroupId;
    private UUID challengeId;
    private User challenger;
    private User targetPlayer;

    @BeforeEach
    void setUp() {
        challengerId = UUID.randomUUID();
        targetPlayerId = UUID.randomUUID();
        targetGroupId = UUID.randomUUID();
        challengeId = UUID.randomUUID();

        challenger = User.builder()
                .id(challengerId)
                .nickname("ChallengerPro")
                .email("challenger@example.com")
                .build();

        targetPlayer = User.builder()
                .id(targetPlayerId)
                .nickname("TargetPlayer")
                .email("target@example.com")
                .build();
    }

    @Nested
    @DisplayName("ChallengeCreatedEvent Handling (AC1)")
    class ChallengeCreatedTests {

        @Test
        @DisplayName("Should dispatch push notification to individual target player")
        void shouldDispatchPushToIndividualTarget_onChallengeCreated() {
            var event = new ChallengeCreatedEvent(
                    challengeId, challengerId, "ChallengerPro", targetPlayerId, null, MatchType.ONE_VS_ONE);
            when(userRepository.findById(targetPlayerId)).thenReturn(Optional.of(targetPlayer));

            challengeNotificationListener.handleChallengeCreated(event);

            verify(pushNotificationService).sendChallengeCreatedNotification(
                    eq(challengeId), eq("ChallengerPro"), eq(MatchType.ONE_VS_ONE), eq(List.of(targetPlayer)));
        }

        @Test
        @DisplayName("Should dispatch push notification to group members excluding challenger")
        void shouldDispatchPushToGroupMembersExcludingChallenger_onChallengeCreated() {
            var member2Id = UUID.randomUUID();
            var member2 = User.builder().id(member2Id).nickname("Member2").build();
            var group = PlayerGroup.builder()
                    .id(targetGroupId)
                    .name("Dream Team")
                    .creator(targetPlayer)
                    .build();
            var membership1 = PlayerGroupMember.builder().group(group).user(challenger).build();
            var membership2 = PlayerGroupMember.builder().group(group).user(member2).build();
            group.setMembers(List.of(membership1, membership2));

            var event = new ChallengeCreatedEvent(
                    challengeId, challengerId, "ChallengerPro", null, targetGroupId, MatchType.TWO_VS_TWO);
            when(playerGroupRepository.findByIdWithMembers(targetGroupId)).thenReturn(Optional.of(group));

            challengeNotificationListener.handleChallengeCreated(event);

            verify(pushNotificationService).sendChallengeCreatedNotification(
                    eq(challengeId), eq("ChallengerPro"), eq(MatchType.TWO_VS_TWO), eq(List.of(member2)));
        }

        @Test
        @DisplayName("Should isolate and suppress push notification dispatch exceptions (AC6)")
        void shouldSuppressExceptions_whenPushDispatchFails() {
            var event = new ChallengeCreatedEvent(
                    challengeId, challengerId, "ChallengerPro", targetPlayerId, null, MatchType.ONE_VS_ONE);
            when(userRepository.findById(targetPlayerId)).thenReturn(Optional.of(targetPlayer));
            doThrow(new RuntimeException("Push network timeout"))
                    .when(pushNotificationService)
                    .sendChallengeCreatedNotification(any(), any(), any(), any());

            challengeNotificationListener.handleChallengeCreated(event);

            verify(pushNotificationService).sendChallengeCreatedNotification(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("ChallengeAcceptedEvent Handling (AC3)")
    class ChallengeAcceptedTests {

        @Test
        @DisplayName("Should dispatch push notification to challenger upon acceptance")
        void shouldDispatchPushToChallenger_onChallengeAccepted() {
            var event = new ChallengeAcceptedEvent(
                    challengeId, challengerId, targetPlayerId, "TargetPlayer", MatchType.ONE_VS_ONE);
            when(userRepository.findById(challengerId)).thenReturn(Optional.of(challenger));

            challengeNotificationListener.handleChallengeAccepted(event);

            verify(pushNotificationService).sendChallengeAcceptedNotification(
                    eq(challengeId), eq("TargetPlayer"), eq(MatchType.ONE_VS_ONE), eq(challenger));
        }
    }

    @Nested
    @DisplayName("ChallengeDeclinedEvent Handling (AC4)")
    class ChallengeDeclinedTests {

        @Test
        @DisplayName("Should dispatch push notification to challenger upon decline")
        void shouldDispatchPushToChallenger_onChallengeDeclined() {
            var event = new ChallengeDeclinedEvent(
                    challengeId, challengerId, targetPlayerId, "TargetPlayer");
            when(userRepository.findById(challengerId)).thenReturn(Optional.of(challenger));

            challengeNotificationListener.handleChallengeDeclined(event);

            verify(pushNotificationService).sendChallengeDeclinedNotification(
                    eq(challengeId), eq("TargetPlayer"), eq(challenger));
        }
    }
}
