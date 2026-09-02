package com.tictactore.listener;

import com.tictactore.event.TournamentCancelledEvent;
import com.tictactore.event.TournamentStartedEvent;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TournamentNotificationListener {

    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTournamentStarted(TournamentStartedEvent event) {
        try {
            if (event.participantUserIds() != null) {
                for (UUID userId : event.participantUserIds()) {
                    userRepository.findById(userId).ifPresent(recipient ->
                            pushNotificationService.sendTournamentStartedNotification(
                                    event.tournamentId(),
                                    event.tournamentName(),
                                    recipient
                            )
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to process TournamentStartedEvent for tournament {}", event.tournamentId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTournamentCancelled(TournamentCancelledEvent event) {
        try {
            if (event.participantUserIds() != null) {
                for (UUID userId : event.participantUserIds()) {
                    userRepository.findById(userId).ifPresent(recipient ->
                            pushNotificationService.sendTournamentCancelledNotification(
                                    event.tournamentId(),
                                    event.tournamentName(),
                                    event.reason(),
                                    recipient
                            )
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to process TournamentCancelledEvent for tournament {}", event.tournamentId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTournamentStubPartnerAssigned(com.tictactore.event.TournamentStubPartnerAssignedEvent event) {
        try {
            if (event.teammateUserId() != null) {
                userRepository.findById(event.teammateUserId()).ifPresent(recipient ->
                        pushNotificationService.sendTournamentStubPartnerAssignedNotification(
                                event.tournamentId(),
                                "Tournament",
                                event.matchId(),
                                recipient,
                                false
                        )
                );
            }
            if (event.stubPartnerUserId() != null) {
                userRepository.findById(event.stubPartnerUserId()).ifPresent(recipient ->
                        pushNotificationService.sendTournamentStubPartnerAssignedNotification(
                                event.tournamentId(),
                                "Tournament",
                                event.matchId(),
                                recipient,
                                true
                        )
                );
            }
        } catch (Exception e) {
            log.error("Failed to process TournamentStubPartnerAssignedEvent for match {}", event.matchId(), e);
        }
    }
}
