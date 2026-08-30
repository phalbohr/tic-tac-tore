package com.tictactore.listener;

import com.tictactore.event.ChallengeAcceptedEvent;
import com.tictactore.event.ChallengeCreatedEvent;
import com.tictactore.event.ChallengeDeclinedEvent;
import com.tictactore.model.User;
import com.tictactore.repository.PlayerGroupRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeNotificationListener {

    private final UserRepository userRepository;
    private final PlayerGroupRepository playerGroupRepository;
    private final PushNotificationService pushNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeCreated(ChallengeCreatedEvent event) {
        try {
            if (event.targetPlayerId() != null) {
                userRepository.findById(event.targetPlayerId()).ifPresent(targetUser ->
                        pushNotificationService.sendChallengeCreatedNotification(
                                event.challengeId(),
                                event.challengerNickname(),
                                event.matchType(),
                                List.of(targetUser)
                        )
                );
            } else if (event.targetGroupId() != null) {
                playerGroupRepository.findByIdWithMembers(event.targetGroupId()).ifPresent(group -> {
                    List<User> recipients = group.getMembers().stream()
                            .filter(member -> !member.getId().equals(event.challengerId()))
                            .toList();
                    if (!recipients.isEmpty()) {
                        pushNotificationService.sendChallengeCreatedNotification(
                                event.challengeId(),
                                event.challengerNickname(),
                                event.matchType(),
                                recipients
                        );
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to process ChallengeCreatedEvent for challenge {}", event.challengeId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeAccepted(ChallengeAcceptedEvent event) {
        try {
            userRepository.findById(event.challengerId()).ifPresent(challenger ->
                    pushNotificationService.sendChallengeAcceptedNotification(
                            event.challengeId(),
                            event.targetNickname(),
                            event.matchType(),
                            challenger
                    )
            );
        } catch (Exception e) {
            log.error("Failed to process ChallengeAcceptedEvent for challenge {}", event.challengeId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeDeclined(ChallengeDeclinedEvent event) {
        try {
            userRepository.findById(event.challengerId()).ifPresent(challenger ->
                    pushNotificationService.sendChallengeDeclinedNotification(
                            event.challengeId(),
                            event.targetNickname(),
                            challenger
                    )
            );
        } catch (Exception e) {
            log.error("Failed to process ChallengeDeclinedEvent for challenge {}", event.challengeId(), e);
        }
    }
}
