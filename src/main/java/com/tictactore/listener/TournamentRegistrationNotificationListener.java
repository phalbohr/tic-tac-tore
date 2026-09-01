package com.tictactore.listener;

import com.tictactore.event.TournamentInviteAcceptedEvent;
import com.tictactore.event.TournamentInviteCreatedEvent;
import com.tictactore.event.TournamentInviteDeclinedEvent;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TournamentRegistrationNotificationListener {

    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTournamentInviteCreated(TournamentInviteCreatedEvent event) {
        try {
            if (event.partnerId() != null) {
                userRepository.findById(event.partnerId()).ifPresent(partner ->
                        pushNotificationService.sendTournamentInviteNotification(
                                event.tournamentId(),
                                event.tournamentName(),
                                event.inviterNickname(),
                                partner
                        )
                );
            }
        } catch (Exception e) {
            log.error("Failed to process TournamentInviteCreatedEvent for registration {}", event.registrationId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTournamentInviteAccepted(TournamentInviteAcceptedEvent event) {
        try {
            if (event.inviterId() != null) {
                userRepository.findById(event.inviterId()).ifPresent(inviter ->
                        pushNotificationService.sendTournamentInviteAcceptedNotification(
                                event.tournamentId(),
                                event.tournamentName(),
                                event.partnerNickname(),
                                inviter
                        )
                );
            }
        } catch (Exception e) {
            log.error("Failed to process TournamentInviteAcceptedEvent for registration {}", event.registrationId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTournamentInviteDeclined(TournamentInviteDeclinedEvent event) {
        try {
            if (event.inviterId() != null) {
                userRepository.findById(event.inviterId()).ifPresent(inviter ->
                        pushNotificationService.sendTournamentInviteDeclinedNotification(
                                event.tournamentId(),
                                event.tournamentName(),
                                event.partnerNickname(),
                                inviter
                        )
                );
            }
        } catch (Exception e) {
            log.error("Failed to process TournamentInviteDeclinedEvent for registration {}", event.registrationId(), e);
        }
    }
}
