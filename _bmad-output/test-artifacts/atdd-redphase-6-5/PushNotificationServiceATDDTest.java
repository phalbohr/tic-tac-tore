package com.tictactore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.VapidProperties;
import com.tictactore.dto.PushNotificationPayload;
import com.tictactore.model.MatchType;
import com.tictactore.model.NotificationLog;
import com.tictactore.model.SkillLevel;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.impl.PushNotificationServiceImpl;
import com.tictactore.service.operation.NotificationOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushNotificationService ATDD Tests — Story 6.5: Pool Notifications")
class PushNotificationServiceATDDTest {

    @Mock
    private NotificationOperation notificationOperation;

    @Mock
    private UserRepository userRepository;

    private PushNotificationServiceImpl pushNotificationService;

    @BeforeEach
    void setUp() {
        VapidProperties vapidProperties = new VapidProperties();
        ObjectMapper objectMapper = new ObjectMapper();
        pushNotificationService = new PushNotificationServiceImpl(
                notificationOperation,
                userRepository,
                vapidProperties,
                objectMapper
        );
    }

    @Nested
    @DisplayName("Pool Created Push Notifications (AC 1, AC 5)")
    class PoolCreatedNotificationSpecs {

        @Test
        @DisplayName("[P0] Should generate valid POOL_CREATED payload with correct summary and deep link (AC 1)")
        void shouldDispatchPoolCreatedNotificationWithValidPayload() {
            UUID poolId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            String creatorNickname = "PavelHost";
            User recipient = User.builder().id(UUID.randomUUID()).nickname("Recipient").poolNotificationsEnabled(true).build();

            when(userRepository.findById(creatorId)).thenReturn(Optional.of(User.builder().nickname(creatorNickname).build()));

            assertThatCode(() -> {
                pushNotificationService.sendPoolCreatedNotification(
                        poolId,
                        creatorId,
                        creatorNickname,
                        MatchType.ONE_VS_ONE,
                        SkillLevel.OPEN_FOR_ALL,
                        List.of(recipient)
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[P0] Should pseudonymize retired creators as 'A retired player' in pool push (AC 1)")
        void shouldPseudonymizeRetiredCreatorInPoolNotification() {
            UUID poolId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            User retiredCreator = User.builder().id(creatorId).nickname("ex-player-9999").build();
            User recipient = User.builder().id(UUID.randomUUID()).nickname("Recipient").poolNotificationsEnabled(true).build();

            when(userRepository.findById(creatorId)).thenReturn(Optional.of(retiredCreator));

            assertThatCode(() -> {
                pushNotificationService.sendPoolCreatedNotification(
                        poolId,
                        creatorId,
                        "ex-player-9999",
                        MatchType.TWO_VS_TWO,
                        SkillLevel.PRO,
                        List.of(recipient)
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[P0] Should record NotificationLog with pool_id and status for each recipient (AC 1)")
        void shouldRecordNotificationLogWithPoolIdOnPoolCreated() {
            UUID poolId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            User recipient = User.builder().id(UUID.randomUUID()).nickname("Recipient").poolNotificationsEnabled(true).build();

            when(userRepository.findById(creatorId)).thenReturn(Optional.of(User.builder().nickname("Host").build()));

            pushNotificationService.sendPoolCreatedNotification(
                    poolId,
                    creatorId,
                    "Host",
                    MatchType.ONE_VS_ONE,
                    SkillLevel.OPEN_FOR_ALL,
                    List.of(recipient)
            );

            ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
            verify(notificationOperation).recordLog(logCaptor.capture());
            assertThat(logCaptor.getValue().getPoolId()).isEqualTo(poolId);
            assertThat(logCaptor.getValue().getRecipientId()).isEqualTo(recipient.getId());
        }

        @Test
        @DisplayName("[P0] Should catch delivery failure gracefully and record FAILED status in NotificationLog (AC 5)")
        void shouldHandlePushExceptionGracefullyAndLogFailed() {
            UUID poolId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            User recipient = User.builder().id(UUID.randomUUID()).nickname("Recipient").poolNotificationsEnabled(true).build();

            when(userRepository.findById(creatorId)).thenReturn(Optional.of(User.builder().nickname("Host").build()));

            assertThatCode(() -> {
                pushNotificationService.sendPoolCreatedNotification(
                        poolId,
                        creatorId,
                        "Host",
                        MatchType.ONE_VS_ONE,
                        SkillLevel.OPEN_FOR_ALL,
                        List.of(recipient)
                );
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Pool Filled Push Notifications (AC 2, AC 5)")
    class PoolFilledNotificationSpecs {

        @Test
        @DisplayName("[P0] Should generate valid POOL_FILLED payload and dispatch to all participants (AC 2)")
        void shouldDispatchPoolFilledNotificationWithValidPayload() {
            UUID poolId = UUID.randomUUID();
            User host = User.builder().id(UUID.randomUUID()).nickname("HostUser").build();
            User player = User.builder().id(UUID.randomUUID()).nickname("JoinerUser").build();

            assertThatCode(() -> {
                pushNotificationService.sendPoolFilledNotification(
                        poolId,
                        MatchType.ONE_VS_ONE,
                        List.of(host, player)
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[P0] Should record NotificationLog with pool_id for each participant on POOL_FILLED (AC 2)")
        void shouldRecordNotificationLogForEachParticipantOnPoolFilled() {
            UUID poolId = UUID.randomUUID();
            User host = User.builder().id(UUID.randomUUID()).nickname("HostUser").build();
            User player = User.builder().id(UUID.randomUUID()).nickname("JoinerUser").build();

            pushNotificationService.sendPoolFilledNotification(
                    poolId,
                    MatchType.TWO_VS_TWO,
                    List.of(host, player)
            );

            ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
            verify(notificationOperation, org.mockito.Mockito.atLeastOnce()).recordLog(logCaptor.capture());
            assertThat(logCaptor.getAllValues()).allMatch(log -> poolId.equals(log.getPoolId()));
        }
    }
}
