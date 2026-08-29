package com.tictactore.listener;

import com.tictactore.event.PoolCreatedEvent;
import com.tictactore.event.PoolFilledEvent;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PoolNotificationListener {

    private static final int BATCH_SIZE = 100;

    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePoolCreated(PoolCreatedEvent event) {
        try {
            int pageNumber = 0;
            Slice<User> slice;
            do {
                slice = userRepository.findByPoolNotificationsEnabledTrueAndIdNot(
                        event.creatorId(),
                        PageRequest.of(pageNumber, BATCH_SIZE, Sort.by("id"))
                );
                if (slice.hasContent()) {
                    pushNotificationService.sendPoolCreatedNotification(
                            event.poolId(),
                            event.creatorId(),
                            event.creatorNickname(),
                            event.matchType(),
                            event.skillLevel(),
                            slice.getContent()
                    );
                }
                pageNumber++;
            } while (slice.hasNext());
        } catch (Exception e) {
            log.error("Failed to process PoolCreatedEvent for pool {}", event.poolId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePoolFilled(PoolFilledEvent event) {
        try {
            List<User> participants = userRepository.findAllById(event.participantUserIds());
            pushNotificationService.sendPoolFilledNotification(
                    event.poolId(),
                    event.matchType(),
                    participants
            );
        } catch (Exception e) {
            log.error("Failed to process PoolFilledEvent for pool {}", event.poolId(), e);
        }
    }
}
