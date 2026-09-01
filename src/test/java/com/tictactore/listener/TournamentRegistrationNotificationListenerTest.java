package com.tictactore.listener;

import com.tictactore.event.TournamentInviteAcceptedEvent;
import com.tictactore.event.TournamentInviteCreatedEvent;
import com.tictactore.event.TournamentInviteDeclinedEvent;
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

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentRegistrationNotificationListener Unit Tests")
class TournamentRegistrationNotificationListenerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private TournamentRegistrationNotificationListener listener;

    private final UUID registrationId = UUID.randomUUID();
    private final UUID tournamentId = UUID.randomUUID();
    private final UUID inviterId = UUID.randomUUID();
    private final UUID partnerId = UUID.randomUUID();

    @Nested
    @DisplayName("TournamentInviteCreatedEvent Processing")
    class TournamentInviteCreatedEventSpecs {

        @Test
        void shouldSendNotificationToPartner_whenInviteCreated() {
            var partner = User.builder().id(partnerId).nickname("Partner").build();
            var event = new TournamentInviteCreatedEvent(registrationId, tournamentId, "Cup 2026", inviterId, "Inviter", partnerId);
            when(userRepository.findById(partnerId)).thenReturn(Optional.of(partner));

            listener.handleTournamentInviteCreated(event);

            verify(pushNotificationService).sendTournamentInviteNotification(tournamentId, "Cup 2026", "Inviter", partner);
        }
    }

    @Nested
    @DisplayName("TournamentInviteAcceptedEvent Processing")
    class TournamentInviteAcceptedEventSpecs {

        @Test
        void shouldSendNotificationToInviter_whenInviteAccepted() {
            var inviter = User.builder().id(inviterId).nickname("Inviter").build();
            var event = new TournamentInviteAcceptedEvent(registrationId, tournamentId, "Cup 2026", partnerId, "Partner", inviterId);
            when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviter));

            listener.handleTournamentInviteAccepted(event);

            verify(pushNotificationService).sendTournamentInviteAcceptedNotification(tournamentId, "Cup 2026", "Partner", inviter);
        }
    }

    @Nested
    @DisplayName("TournamentInviteDeclinedEvent Processing")
    class TournamentInviteDeclinedEventSpecs {

        @Test
        void shouldSendNotificationToInviter_whenInviteDeclined() {
            var inviter = User.builder().id(inviterId).nickname("Inviter").build();
            var event = new TournamentInviteDeclinedEvent(registrationId, tournamentId, "Cup 2026", partnerId, "Partner", inviterId);
            when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviter));

            listener.handleTournamentInviteDeclined(event);

            verify(pushNotificationService).sendTournamentInviteDeclinedNotification(tournamentId, "Cup 2026", "Partner", inviter);
        }
    }
}
