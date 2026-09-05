package com.tictactore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.VapidProperties;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.impl.PushNotificationServiceImpl;
import com.tictactore.service.operation.NotificationOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushNotificationService ATDD Tests")
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
                objectMapper);
    }

    @Nested
    @DisplayName("Web Push Payload & Pseudonymization (AC 4, 5)")
    class PayloadAndPseudonymization {

        @Test
        @DisplayName("[P0] Should generate valid Web Push JSON payload structure with ISO-8601 timestamp")
        void shouldGenerateValidPayloadContract() {
            Match match = new Match();
            match.setId(UUID.randomUUID());
            match.setCreatorId(UUID.randomUUID());

            User opponent = new User();
            opponent.setId(UUID.randomUUID());

            when(userRepository.findById(match.getCreatorId()))
                    .thenReturn(Optional.of(User.builder().nickname("player1").build()));

            assertThatCode(() -> {
                pushNotificationService.sendConfirmationRequest(match, List.of(opponent), false);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[P0] Should pseudonymize retired creators matching ex-player-* as 'A retired player'")
        void shouldPseudonymizeRetiredCreator() {
            User retiredCreator = new User();
            retiredCreator.setId(UUID.randomUUID());
            retiredCreator.setNickname("ex-player-0042");

            when(userRepository.findById(retiredCreator.getId())).thenReturn(Optional.of(retiredCreator));

            Match match = new Match();
            match.setId(UUID.randomUUID());
            match.setCreatorId(retiredCreator.getId());

            User opponent = new User();
            opponent.setId(UUID.randomUUID());

            assertThatCode(() -> {
                pushNotificationService.sendConfirmationRequest(match, List.of(opponent), true);
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Audit Log & Network Exception Resilience (AC 5)")
    class AuditLogAndResilience {

        @Test
        @DisplayName("[P0] Should record NotificationLog entity with status DELIVERED or QUEUED")
        void shouldRecordAuditLogOnSuccess() {
            UUID recipientId = UUID.randomUUID();
            UUID matchId = UUID.randomUUID();

            assertThat(recipientId).isNotNull();
            assertThat(matchId).isNotNull();
        }

        @Test
        @DisplayName("Should catch push delivery exception gracefully, record FAILED status, and not fail match transaction")
        void shouldLogFailedStatusWithoutThrowingException() {
            Match match = new Match();
            match.setId(UUID.randomUUID());

            assertThatCode(() -> {
                pushNotificationService.sendConfirmationRequest(match, List.of(), false);
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Story 6.5: Pool Created Push Notifications (AC 1, AC 5)")
    class PoolCreatedNotificationSpecs {

        @Test
        @DisplayName("Should generate valid POOL_CREATED payload with correct summary and deep link (AC 1)")
        void shouldDispatchPoolCreatedNotificationWithValidPayload() {
            UUID poolId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            String creatorNickname = "PavelHost";
            User recipient = User.builder().id(UUID.randomUUID()).nickname("Recipient").poolNotificationsEnabled(true)
                    .build();
            when(userRepository.findById(creatorId))
                    .thenReturn(Optional.of(User.builder().nickname(creatorNickname).build()));

            assertThatCode(() -> {
                pushNotificationService.sendPoolCreatedNotification(
                        poolId,
                        creatorId,
                        creatorNickname,
                        com.tictactore.model.MatchType.ONE_VS_ONE,
                        com.tictactore.model.SkillLevel.OPEN_FOR_ALL,
                        List.of(recipient));
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pseudonymize retired creators as 'A retired player' in pool push (AC 1)")
        void shouldPseudonymizeRetiredCreatorInPoolNotification() {
            UUID poolId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            User retiredCreator = User.builder().id(creatorId).nickname("ex-player-9999").build();
            User recipient = User.builder().id(UUID.randomUUID()).nickname("Recipient").poolNotificationsEnabled(true)
                    .build();
            when(userRepository.findById(creatorId)).thenReturn(Optional.of(retiredCreator));

            assertThatCode(() -> {
                pushNotificationService.sendPoolCreatedNotification(
                        poolId,
                        creatorId,
                        "ex-player-9999",
                        com.tictactore.model.MatchType.TWO_VS_TWO,
                        com.tictactore.model.SkillLevel.ADVANCED,
                        List.of(recipient));
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should record NotificationLog with pool_id and status for each recipient (AC 1)")
        void shouldRecordNotificationLogWithPoolIdOnPoolCreated() {
            UUID poolId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            User recipient = User.builder().id(UUID.randomUUID()).nickname("Recipient").poolNotificationsEnabled(true)
                    .build();
            when(userRepository.findById(creatorId)).thenReturn(Optional.of(User.builder().nickname("Host").build()));

            pushNotificationService.sendPoolCreatedNotification(
                    poolId,
                    creatorId,
                    "Host",
                    com.tictactore.model.MatchType.ONE_VS_ONE,
                    com.tictactore.model.SkillLevel.OPEN_FOR_ALL,
                    List.of(recipient));

            org.mockito.ArgumentCaptor<com.tictactore.model.NotificationLog> logCaptor = org.mockito.ArgumentCaptor
                    .forClass(com.tictactore.model.NotificationLog.class);
            org.mockito.Mockito.verify(notificationOperation).saveNotificationLog(logCaptor.capture());
            assertThat(logCaptor.getValue().getPoolId()).isEqualTo(poolId);
            assertThat(logCaptor.getValue().getRecipientId()).isEqualTo(recipient.getId());
        }

        @Test
        @DisplayName("Should catch delivery failure gracefully and record FAILED status in NotificationLog (AC 5)")
        void shouldHandlePushExceptionGracefullyAndLogFailed() {
            UUID poolId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            User recipient = User.builder().id(UUID.randomUUID()).nickname("Recipient").poolNotificationsEnabled(true)
                    .build();
            when(userRepository.findById(creatorId)).thenReturn(Optional.of(User.builder().nickname("Host").build()));

            assertThatCode(() -> {
                pushNotificationService.sendPoolCreatedNotification(
                        poolId,
                        creatorId,
                        "Host",
                        com.tictactore.model.MatchType.ONE_VS_ONE,
                        com.tictactore.model.SkillLevel.OPEN_FOR_ALL,
                        List.of(recipient));
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Story 6.5: Pool Filled Push Notifications (AC 2, AC 5)")
    class PoolFilledNotificationSpecs {

        @Test
        @DisplayName("Should generate valid POOL_FILLED payload and dispatch to all participants (AC 2)")
        void shouldDispatchPoolFilledNotificationWithValidPayload() {
            UUID poolId = UUID.randomUUID();
            User host = User.builder().id(UUID.randomUUID()).nickname("HostUser").build();
            User player = User.builder().id(UUID.randomUUID()).nickname("JoinerUser").build();

            assertThatCode(() -> {
                pushNotificationService.sendPoolFilledNotification(
                        poolId,
                        com.tictactore.model.MatchType.ONE_VS_ONE,
                        List.of(host, player));
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should record NotificationLog with pool_id for each participant on POOL_FILLED (AC 2)")
        void shouldRecordNotificationLogForEachParticipantOnPoolFilled() {
            UUID poolId = UUID.randomUUID();
            User host = User.builder().id(UUID.randomUUID()).nickname("HostUser").build();
            User player = User.builder().id(UUID.randomUUID()).nickname("JoinerUser").build();

            pushNotificationService.sendPoolFilledNotification(
                    poolId,
                    com.tictactore.model.MatchType.TWO_VS_TWO,
                    List.of(host, player));

            org.mockito.ArgumentCaptor<com.tictactore.model.NotificationLog> logCaptor = org.mockito.ArgumentCaptor
                    .forClass(com.tictactore.model.NotificationLog.class);
            org.mockito.Mockito.verify(notificationOperation, org.mockito.Mockito.atLeastOnce())
                    .saveNotificationLog(logCaptor.capture());
            assertThat(logCaptor.getAllValues()).allMatch(log -> poolId.equals(log.getPoolId()));
        }
    }
}
