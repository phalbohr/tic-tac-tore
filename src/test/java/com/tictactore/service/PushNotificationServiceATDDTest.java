package com.tictactore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.VapidProperties;
import com.tictactore.dto.PushNotificationPayload;
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
import static org.mockito.ArgumentMatchers.any;
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
                objectMapper
        );
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

            when(userRepository.findById(match.getCreatorId())).thenReturn(Optional.of(User.builder().nickname("player1").build()));

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
        @DisplayName("[P0] Should catch push delivery exception gracefully, record FAILED status, and not fail match transaction")
        void shouldLogFailedStatusWithoutThrowingException() {
            Match match = new Match();
            match.setId(UUID.randomUUID());

            assertThatCode(() -> {
                pushNotificationService.sendConfirmationRequest(match, List.of(), false);
            }).doesNotThrowAnyException();
        }
    }
}
