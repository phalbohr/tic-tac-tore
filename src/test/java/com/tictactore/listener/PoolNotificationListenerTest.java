package com.tictactore.listener;

import com.tictactore.event.PoolCreatedEvent;
import com.tictactore.event.PoolFilledEvent;
import com.tictactore.model.MatchType;
import com.tictactore.model.SkillLevel;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.PushNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PoolNotificationListener ATDD Tests — Story 6.5: Pool Notifications")
class PoolNotificationListenerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private PoolNotificationListener poolNotificationListener;

    @Nested
    @DisplayName("PoolCreatedEvent Processing (AC 1, AC 4)")
    class PoolCreatedEventTests {

        @Test
        @DisplayName("Should query eligible subscribers excluding creator and dispatch push notification in batches (AC 1)")
        void shouldDispatchPushToEligibleSubscribersExcludingCreator() {
            UUID poolId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            String creatorNickname = "PavelHost";
            User recipient1 = User.builder().id(UUID.randomUUID()).nickname("PlayerA").poolNotificationsEnabled(true).build();
            User recipient2 = User.builder().id(UUID.randomUUID()).nickname("PlayerB").poolNotificationsEnabled(true).build();
            List<User> eligibleUsers = List.of(recipient1, recipient2);
            Slice<User> slice = new SliceImpl<>(eligibleUsers, PageRequest.of(0, 100, Sort.by("id")), false);
            when(userRepository.findByPoolNotificationsEnabledTrueAndIdNot(eq(creatorId), any())).thenReturn(slice);
            PoolCreatedEvent event = new PoolCreatedEvent(poolId, creatorId, MatchType.ONE_VS_ONE, SkillLevel.OPEN_FOR_ALL, creatorNickname);

            poolNotificationListener.handlePoolCreated(event);

            verify(userRepository).findByPoolNotificationsEnabledTrueAndIdNot(eq(creatorId), eq(PageRequest.of(0, 100, Sort.by("id"))));
            verify(pushNotificationService).sendPoolCreatedNotification(
                    eq(poolId),
                    eq(creatorId),
                    eq(creatorNickname),
                    eq(MatchType.ONE_VS_ONE),
                    eq(SkillLevel.OPEN_FOR_ALL),
                    eq(eligibleUsers)
            );
        }

        @Test
        @DisplayName("Should not dispatch push notifications when no eligible subscribers found")
        void shouldNotDispatchWhenNoEligibleSubscribers() {
            UUID poolId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            Slice<User> emptySlice = new SliceImpl<>(List.of(), PageRequest.of(0, 100, Sort.by("id")), false);
            when(userRepository.findByPoolNotificationsEnabledTrueAndIdNot(eq(creatorId), any())).thenReturn(emptySlice);
            PoolCreatedEvent event = new PoolCreatedEvent(poolId, creatorId, MatchType.TWO_VS_TWO, SkillLevel.ADVANCED, "Host");

            poolNotificationListener.handlePoolCreated(event);

            verify(pushNotificationService, never()).sendPoolCreatedNotification(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should catch any push service exceptions gracefully without bubbling up (AC 5)")
        void shouldHandlePushExceptionsGracefullyOnPoolCreated() {
            UUID poolId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            User recipient = User.builder().id(UUID.randomUUID()).nickname("PlayerA").poolNotificationsEnabled(true).build();
            Slice<User> slice = new SliceImpl<>(List.of(recipient), PageRequest.of(0, 100, Sort.by("id")), false);
            when(userRepository.findByPoolNotificationsEnabledTrueAndIdNot(eq(creatorId), any())).thenReturn(slice);
            doThrow(new RuntimeException("Push dispatch failed"))
                    .when(pushNotificationService)
                    .sendPoolCreatedNotification(any(), any(), any(), any(), any(), any());
            PoolCreatedEvent event = new PoolCreatedEvent(poolId, creatorId, MatchType.ONE_VS_ONE, SkillLevel.OPEN_FOR_ALL, "Host");

            assertThatCode(() -> poolNotificationListener.handlePoolCreated(event))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("PoolFilledEvent Processing (AC 2)")
    class PoolFilledEventTests {

        @Test
        @DisplayName("Should fetch participants and dispatch POOL_FILLED push notifications (AC 2)")
        void shouldDispatchPoolFilledPushToAllParticipants() {
            UUID poolId = UUID.randomUUID();
            UUID hostId = UUID.randomUUID();
            UUID playerId = UUID.randomUUID();
            List<UUID> participantIds = List.of(hostId, playerId);
            User host = User.builder().id(hostId).nickname("HostUser").build();
            User player = User.builder().id(playerId).nickname("JoinerUser").build();
            when(userRepository.findAllById(participantIds)).thenReturn(List.of(host, player));
            PoolFilledEvent event = new PoolFilledEvent(poolId, MatchType.ONE_VS_ONE, participantIds);

            poolNotificationListener.handlePoolFilled(event);

            verify(userRepository).findAllById(participantIds);
            verify(pushNotificationService).sendPoolFilledNotification(
                    eq(poolId),
                    eq(MatchType.ONE_VS_ONE),
                    eq(List.of(host, player))
            );
        }

        @Test
        @DisplayName("Should catch any push service exceptions gracefully on POOL_FILLED (AC 5)")
        void shouldHandlePushExceptionsGracefullyOnPoolFilled() {
            UUID poolId = UUID.randomUUID();
            UUID hostId = UUID.randomUUID();
            List<UUID> participantIds = List.of(hostId);
            User host = User.builder().id(hostId).nickname("HostUser").build();
            when(userRepository.findAllById(participantIds)).thenReturn(List.of(host));
            doThrow(new RuntimeException("Web push connection timed out"))
                    .when(pushNotificationService)
                    .sendPoolFilledNotification(any(), any(), any());
            PoolFilledEvent event = new PoolFilledEvent(poolId, MatchType.ONE_VS_ONE, participantIds);

            assertThatCode(() -> poolNotificationListener.handlePoolFilled(event))
                    .doesNotThrowAnyException();
        }
    }
}
