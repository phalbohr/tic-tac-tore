package com.tictactore.listener;

import com.tictactore.event.TournamentCancelledEvent;
import com.tictactore.event.TournamentStartedEvent;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.PushNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentNotificationListener Unit Tests")
class TournamentNotificationListenerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private TournamentNotificationListener listener;

    @Test
    void shouldSendStartedNotificationToAllParticipants() {
        UUID tournamentId = UUID.randomUUID();
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        User user1 = User.builder().id(u1).nickname("User1").build();
        User user2 = User.builder().id(u2).nickname("User2").build();

        when(userRepository.findById(u1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(u2)).thenReturn(Optional.of(user2));

        var event = new TournamentStartedEvent(
                tournamentId,
                "Spring Open",
                TournamentFormat.CUP,
                TournamentMode.ONE_VS_ONE_PERSONAL,
                List.of(u1, u2),
                7
        );

        listener.handleTournamentStarted(event);

        verify(pushNotificationService).sendTournamentStartedNotification(tournamentId, "Spring Open", user1);
        verify(pushNotificationService).sendTournamentStartedNotification(tournamentId, "Spring Open", user2);
    }

    @Test
    void shouldSendCancelledNotificationToAllParticipants() {
        UUID tournamentId = UUID.randomUUID();
        UUID u1 = UUID.randomUUID();

        User user1 = User.builder().id(u1).nickname("User1").build();
        when(userRepository.findById(u1)).thenReturn(Optional.of(user1));

        var event = new TournamentCancelledEvent(
                tournamentId,
                "Spring Open",
                "Low capacity",
                List.of(u1)
        );

        listener.handleTournamentCancelled(event);

        verify(pushNotificationService).sendTournamentCancelledNotification(tournamentId, "Spring Open", "Low capacity", user1);
    }
}
