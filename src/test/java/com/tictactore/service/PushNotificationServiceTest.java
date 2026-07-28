package com.tictactore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.VapidProperties;
import com.tictactore.dto.PushNotificationPayload;
import com.tictactore.dto.PushSubscriptionRequest;
import com.tictactore.model.Match;
import com.tictactore.model.NotificationLog;
import com.tictactore.model.PushSubscription;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.impl.PushNotificationServiceImpl;
import com.tictactore.service.operation.NotificationOperation;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushNotificationService Unit Tests")
class PushNotificationServiceTest {

    @Mock
    private NotificationOperation notificationOperation;

    @Mock
    private UserRepository userRepository;

    private PushNotificationServiceImpl pushNotificationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        VapidProperties vapidProperties = new VapidProperties();
        objectMapper = new ObjectMapper();
        pushNotificationService = new PushNotificationServiceImpl(
                notificationOperation,
                userRepository,
                vapidProperties,
                objectMapper
        );
    }

    @Nested
    @DisplayName("Subscription Management")
    class SubscriptionManagement {

        @Test
        @DisplayName("[P0] subscribe() should delegate saveSubscription to NotificationOperation")
        void shouldDelegateSubscriptionToOperation() {
            UUID userId = UUID.randomUUID();
            PushSubscriptionRequest request = new PushSubscriptionRequest("https://push.example.com", "p256dh", "auth");

            pushNotificationService.subscribe(userId, request);

            verify(notificationOperation).saveSubscription(userId, request.endpoint(), request.p256dh(), request.auth());
        }

        @Test
        @DisplayName("[P0] unsubscribe() should delegate deleteSubscription to NotificationOperation")
        void shouldDelegateUnsubscriptionToOperation() {
            UUID userId = UUID.randomUUID();
            String endpoint = "https://push.services.mozilla.com/push/v1/gAAAAA...";

            pushNotificationService.unsubscribe(userId, endpoint);

            verify(notificationOperation).deleteSubscription(userId, endpoint);
        }
    }

    @Nested
    @DisplayName("Confirmation Request Dispatch")
    class ConfirmationRequestDispatch {

        @Test
        @DisplayName("[P0] Should record SKIPPED audit log when opponent has no push subscriptions")
        void shouldSkipNotificationWhenNoSubscriptions() {
            UUID opponentId = UUID.randomUUID();
            User opponent = new User();
            opponent.setId(opponentId);

            Match match = new Match();
            match.setId(UUID.randomUUID());
            match.setCreatorId(UUID.randomUUID());

            when(notificationOperation.getSubscriptionsForUser(opponentId)).thenReturn(List.of());

            pushNotificationService.sendConfirmationRequest(match, List.of(opponent), false);

            ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
            verify(notificationOperation, times(1)).saveNotificationLog(captor.capture());
            NotificationLog log = captor.getValue();
            assertThat(log.getStatus()).isEqualTo("SKIPPED");
            assertThat(log.getType()).isEqualTo("CONFIRMATION_REQUEST");
            assertThat(log.getRecipientId()).isEqualTo(opponentId);
            assertThat(log.getMatchId()).isEqualTo(match.getId());
        }

        @Test
        @DisplayName("[P0] Should not throw when opponents list is empty")
        void shouldNotThrowOnEmptyOpponents() {
            Match match = new Match();
            match.setId(UUID.randomUUID());

            assertThatCode(() -> pushNotificationService.sendConfirmationRequest(match, List.of(), false))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[P1] Should dispatch to all subscriptions for a single opponent")
        void shouldDispatchToAllSubscriptionsForOpponent() {
            UUID recipientId = UUID.randomUUID();
            User opponent = new User();
            opponent.setId(recipientId);

            Match match = new Match();
            match.setId(UUID.randomUUID());
            match.setCreatorId(UUID.randomUUID());

            PushSubscription sub1 = PushSubscription.builder()
                    .id(UUID.randomUUID())
                    .userId(recipientId)
                    .endpoint("https://push.example.com/1")
                    .p256dh("dGVzdA==")
                    .auth("dGVzdA==")
                    .build();
            PushSubscription sub2 = PushSubscription.builder()
                    .id(UUID.randomUUID())
                    .userId(recipientId)
                    .endpoint("https://push.example.com/2")
                    .p256dh("dGVzdA==")
                    .auth("dGVzdA==")
                    .build();

            when(notificationOperation.getSubscriptionsForUser(recipientId)).thenReturn(List.of(sub1, sub2));

            pushNotificationService.sendConfirmationRequest(match, List.of(opponent), false);

            verify(notificationOperation, times(2)).saveNotificationLog(any(NotificationLog.class));
        }
    }

    @Nested
    @DisplayName("Payload Contract & Pseudonymization")
    class PayloadContractAndPseudonymization {

        @Test
        @DisplayName("[P0] Should generate payload with exact JSON contract fields")
        void shouldGenerateExactPayloadContract() {
            UUID matchId = UUID.randomUUID();
            UUID creatorId = UUID.randomUUID();
            Match match = new Match();
            match.setId(matchId);
            match.setCreatorId(creatorId);
            match.setGames(List.of());

            User creator = User.builder().id(creatorId).nickname("player1").build();
            User opponent = new User();
            opponent.setId(UUID.randomUUID());

            when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
            when(notificationOperation.getSubscriptionsForUser(any())).thenReturn(List.of());

            try (MockedConstruction<PushService> mocked = Mockito.mockConstruction(PushService.class)) {
                pushNotificationService.sendConfirmationRequest(match, List.of(opponent), true);

                ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
                verify(notificationOperation, times(1)).saveNotificationLog(captor.capture());
                NotificationLog log = captor.getValue();
                assertThat(log.getPayload()).isNotNull();

                PushNotificationPayload payload = objectMapper.readValue(log.getPayload(), PushNotificationPayload.class);
                assertThat(payload.matchId()).isEqualTo(matchId);
                assertThat(payload.creatorName()).isEqualTo("player1");
                assertThat(payload.isDuplicateWarning()).isTrue();
                assertThat(payload.timestamp()).isNotNull();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("[P0] Should pseudonymize ex-player-* creators as 'A retired player'")
        void shouldPseudonymizeExPlayerCreators() {
            UUID creatorId = UUID.randomUUID();
            Match match = new Match();
            match.setId(UUID.randomUUID());
            match.setCreatorId(creatorId);
            match.setGames(List.of());

            User retiredCreator = User.builder().id(creatorId).nickname("ex-player-0042").build();
            User opponent = new User();
            opponent.setId(UUID.randomUUID());

            when(userRepository.findById(creatorId)).thenReturn(Optional.of(retiredCreator));
            when(notificationOperation.getSubscriptionsForUser(any())).thenReturn(List.of());

            try (MockedConstruction<PushService> mocked = Mockito.mockConstruction(PushService.class)) {
                pushNotificationService.sendConfirmationRequest(match, List.of(opponent), false);

                ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
                verify(notificationOperation, times(1)).saveNotificationLog(captor.capture());
                NotificationLog log = captor.getValue();

                PushNotificationPayload payload = objectMapper.readValue(log.getPayload(), PushNotificationPayload.class);
                assertThat(payload.creatorName()).isEqualTo("A retired player");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("[P0] Should pseudonymize null creatorId as 'A retired player'")
        void shouldPseudonymizeNullCreator() {
            Match match = new Match();
            match.setId(UUID.randomUUID());
            match.setCreatorId(null);
            match.setGames(List.of());

            User opponent = new User();
            opponent.setId(UUID.randomUUID());

            when(notificationOperation.getSubscriptionsForUser(any())).thenReturn(List.of());

            try (MockedConstruction<PushService> mocked = Mockito.mockConstruction(PushService.class)) {
                pushNotificationService.sendConfirmationRequest(match, List.of(opponent), false);

                ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
                verify(notificationOperation, times(1)).saveNotificationLog(captor.capture());
                NotificationLog log = captor.getValue();

                PushNotificationPayload payload = objectMapper.readValue(log.getPayload(), PushNotificationPayload.class);
                assertThat(payload.creatorName()).isEqualTo("A retired player");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("[P0] Should preserve regular nicknames without pseudonymization")
        void shouldPreserveRegularNicknames() {
            UUID creatorId = UUID.randomUUID();
            Match match = new Match();
            match.setId(UUID.randomUUID());
            match.setCreatorId(creatorId);
            match.setGames(List.of());

            User creator = User.builder().id(creatorId).nickname("alice").build();

            when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
            when(notificationOperation.getSubscriptionsForUser(any())).thenReturn(List.of());

            try (MockedConstruction<PushService> mocked = Mockito.mockConstruction(PushService.class)) {
                pushNotificationService.sendConfirmationRequest(match, List.of(new User()), false);

                ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
                verify(notificationOperation, times(1)).saveNotificationLog(captor.capture());
                NotificationLog log = captor.getValue();

                PushNotificationPayload payload = objectMapper.readValue(log.getPayload(), PushNotificationPayload.class);
                assertThat(payload.creatorName()).isEqualTo("alice");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("[P0] Should generate summary with correct game count pluralization")
        void shouldGenerateCorrectGameSummary() {
            UUID creatorId = UUID.randomUUID();
            Match match = new Match();
            match.setId(UUID.randomUUID());
            match.setCreatorId(creatorId);
            match.setGames(List.of());

            User creator = User.builder().id(creatorId).nickname("player1").build();

            when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
            when(notificationOperation.getSubscriptionsForUser(any())).thenReturn(List.of());

            try (MockedConstruction<PushService> mocked = Mockito.mockConstruction(PushService.class)) {
                pushNotificationService.sendConfirmationRequest(match, List.of(new User()), false);

                ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
                verify(notificationOperation, times(1)).saveNotificationLog(captor.capture());
                NotificationLog log = captor.getValue();

                PushNotificationPayload payload = objectMapper.readValue(log.getPayload(), PushNotificationPayload.class);
                assertThat(payload.summary()).isEqualTo("0 games submitted");
            } catch (Exception e) {
                throw new RuntimeException(e);
        }
    }
}
}
