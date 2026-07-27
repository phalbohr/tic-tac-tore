package com.tictactore.service.operation;

import com.tictactore.annotation.Idempotent;
import com.tictactore.model.NotificationLog;
import com.tictactore.model.PushSubscription;
import com.tictactore.repository.NotificationLogRepository;
import com.tictactore.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationOperation {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final NotificationLogRepository notificationLogRepository;

    @Idempotent
    @Transactional
    public PushSubscription saveSubscription(UUID userId, String endpoint, String p256dh, String auth) {
        var existing = pushSubscriptionRepository.findByUserIdAndEndpoint(userId, endpoint);
        if (existing.isPresent()) {
            var sub = existing.get();
            sub.setP256dh(p256dh);
            sub.setAuth(auth);
            return pushSubscriptionRepository.save(sub);
        }
        var sub = PushSubscription.builder()
                .userId(userId)
                .endpoint(endpoint)
                .p256dh(p256dh)
                .auth(auth)
                .createdAt(Instant.now())
                .build();
        return pushSubscriptionRepository.save(sub);
    }

    @Idempotent
    @Transactional
    public void deleteSubscription(UUID userId, String endpoint) {
        pushSubscriptionRepository.deleteByUserIdAndEndpoint(userId, endpoint);
    }

    @Transactional(readOnly = true)
    public List<PushSubscription> getSubscriptionsForUser(UUID userId) {
        return pushSubscriptionRepository.findByUserId(userId);
    }

    @Idempotent
    @Transactional
    public NotificationLog saveNotificationLog(NotificationLog log) {
        return notificationLogRepository.save(log);
    }
}
