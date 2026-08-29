package com.tictactore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.VapidProperties;
import com.tictactore.dto.NotificationLogDto;
import com.tictactore.dto.PushNotificationPayload;
import com.tictactore.dto.PushSubscriptionRequest;
import com.tictactore.model.NotificationLog;
import com.tictactore.model.Match;
import com.tictactore.model.PushSubscription;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.PushNotificationService;
import com.tictactore.service.operation.NotificationOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Security;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
@Retryable
public class PushNotificationServiceImpl implements PushNotificationService {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final NotificationOperation notificationOperation;
    private final UserRepository userRepository;
    private final VapidProperties vapidProperties;
    private final ObjectMapper objectMapper;

    private PushService cachedPushService;

    private synchronized PushService getPushService() throws Exception {
        if (cachedPushService == null) {
            cachedPushService = new PushService(
                    vapidProperties.getPublicKey(),
                    vapidProperties.getPrivateKey(),
                    vapidProperties.getSubject()
            );
        }
        return cachedPushService;
    }

    @Override
    public void subscribe(UUID userId, PushSubscriptionRequest request) {
        notificationOperation.saveSubscription(userId, request.endpoint(), request.p256dh(), request.auth());
    }

    @Override
    public void unsubscribe(UUID userId, String endpoint) {
        notificationOperation.deleteSubscription(userId, endpoint);
    }

    @Override
    public void sendConfirmationRequest(Match match, List<User> opponents, boolean isDuplicateWarning) {
        String creatorName = resolveCreatorName(match.getCreatorId());
        String summary = formatMatchSummary(match);
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = new PushNotificationPayload(
                match.getId(),
                creatorName,
                summary,
                isDuplicateWarning,
                timestamp
        );

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize push notification payload for match {}", match.getId(), e);
            return;
        }

        for (User opponent : opponents) {
            List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(opponent.getId());
            if (subscriptions.isEmpty()) {
                recordNotificationLog(opponent.getId(), match.getId(), "CONFIRMATION_REQUEST", jsonPayload, "SKIPPED", "No push subscription registered");
                continue;
            }

            for (PushSubscription sub : subscriptions) {
                dispatchPushNotification(sub, opponent.getId(), match.getId(), "CONFIRMATION_REQUEST", jsonPayload);
            }
        }
    }

    @Override
    public void sendRejectionNotification(Match match, User creator, String rejectionReason) {
        if (creator == null || creator.getId() == null) {
            return;
        }
        String opponentName = resolveCreatorName(match.getRejectedByUserId());
        String summary = opponentName + " rejected your match. Reason: " + rejectionReason;
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = new PushNotificationPayload(
                match.getId(),
                opponentName,
                summary,
                false,
                timestamp
        );

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize rejection push notification payload for match {}", match.getId(), e);
            return;
        }

        List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(creator.getId());
        if (subscriptions.isEmpty()) {
            recordNotificationLog(creator.getId(), match.getId(), "MATCH_REJECTED", jsonPayload, "SKIPPED", "No push subscription registered");
            return;
        }

        for (PushSubscription sub : subscriptions) {
            dispatchPushNotification(sub, creator.getId(), match.getId(), "MATCH_REJECTED", jsonPayload);
        }
    }

    @Override
    public void sendPartialConfirmationNotification(Match match, List<User> remainingOpponents, String firstConfirmerName) {
        String summary = firstConfirmerName + " confirmed your match. Waiting for " + remainingOpponents.size() + " more opponent" + (remainingOpponents.size() == 1 ? "" : "s") + " to confirm.";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = new PushNotificationPayload(
                match.getId(),
                firstConfirmerName,
                summary,
                false,
                timestamp
        );

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize partial confirmation push notification payload for match {}", match.getId(), e);
            return;
        }

        for (User opponent : remainingOpponents) {
            List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(opponent.getId());
            if (subscriptions.isEmpty()) {
                recordNotificationLog(opponent.getId(), match.getId(), "PARTIAL_CONFIRMATION", jsonPayload, "SKIPPED", "No push subscription registered");
                continue;
            }
            for (PushSubscription sub : subscriptions) {
                dispatchPushNotification(sub, opponent.getId(), match.getId(), "PARTIAL_CONFIRMATION", jsonPayload);
            }
        }
    }

    @Override
    public void sendCooldownReminderNotification(Match match, List<User> recipients) {
        String summary = "Match " + match.getId() + " is awaiting final confirmation. It will be auto-published when the cooldown expires.";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = new PushNotificationPayload(
                match.getId(),
                "System",
                summary,
                false,
                timestamp
        );

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize cooldown reminder push notification payload for match {}", match.getId(), e);
            return;
        }

        for (User recipient : recipients) {
            List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(recipient.getId());
            if (subscriptions.isEmpty()) {
                recordNotificationLog(recipient.getId(), match.getId(), "COOLDOWN_REMINDER", jsonPayload, "SKIPPED", "No push subscription registered");
                continue;
            }
            for (PushSubscription sub : subscriptions) {
                dispatchPushNotification(sub, recipient.getId(), match.getId(), "COOLDOWN_REMINDER", jsonPayload);
            }
        }
    }

    @Override
    public void sendPoolCreatedNotification(
            UUID poolId,
            UUID creatorId,
            String creatorName,
            com.tictactore.model.MatchType matchType,
            com.tictactore.model.SkillLevel skillLevel,
            List<User> recipients
    ) {
        if (recipients == null || recipients.isEmpty()) {
            return;
        }

        String resolvedCreator = resolveCreatorName(creatorId);
        String matchTypeStr = matchType == com.tictactore.model.MatchType.TWO_VS_TWO ? "2v2" : "1v1";
        String summary = "A new " + matchTypeStr + " pool is looking for players";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = new PushNotificationPayload(
                null,
                poolId,
                "POOL_CREATED",
                resolvedCreator,
                summary,
                "/",
                false,
                timestamp
        );

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize pool created push notification payload for pool {}", poolId, e);
            return;
        }

        for (User recipient : recipients) {
            List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(recipient.getId());
            if (subscriptions.isEmpty()) {
                recordNotificationLog(recipient.getId(), null, poolId, "POOL_CREATED", jsonPayload, "SKIPPED", "No push subscription registered");
                continue;
            }

            for (PushSubscription sub : subscriptions) {
                dispatchPushNotification(sub, recipient.getId(), null, poolId, "POOL_CREATED", jsonPayload);
            }
        }
    }

    @Override
    public void sendPoolFilledNotification(
            UUID poolId,
            com.tictactore.model.MatchType matchType,
            List<User> participants
    ) {
        if (participants == null || participants.isEmpty()) {
            return;
        }

        String matchTypeStr = matchType == com.tictactore.model.MatchType.TWO_VS_TWO ? "2v2" : "1v1";
        String summary = "Your " + matchTypeStr + " pool is full — head to the table!";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = new PushNotificationPayload(
                null,
                poolId,
                "POOL_FILLED",
                "System",
                summary,
                "/",
                false,
                timestamp
        );

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize pool filled push notification payload for pool {}", poolId, e);
            return;
        }

        for (User participant : participants) {
            List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(participant.getId());
            if (subscriptions.isEmpty()) {
                recordNotificationLog(participant.getId(), null, poolId, "POOL_FILLED", jsonPayload, "SKIPPED", "No push subscription registered");
                continue;
            }

            for (PushSubscription sub : subscriptions) {
                dispatchPushNotification(sub, participant.getId(), null, poolId, "POOL_FILLED", jsonPayload);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationLogDto> getUserNotifications(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        return notificationOperation.getNotificationsForUser(userId).stream()
                .map(logEntry -> new NotificationLogDto(
                        logEntry.getId(),
                        logEntry.getRecipientId(),
                        logEntry.getMatchId(),
                        logEntry.getPoolId(),
                        logEntry.getType(),
                        logEntry.getPayload(),
                        logEntry.getStatus(),
                        logEntry.getErrorMessage(),
                        logEntry.getSentAt()
                ))
                .toList();
    }


    private String resolveCreatorName(UUID creatorId) {
        if (creatorId == null) {
            return "A retired player";
        }
        return userRepository.findById(creatorId)
                .map(u -> {
                    if (u.getNickname() != null && u.getNickname().startsWith("ex-player-")) {
                        return "A retired player";
                    }
                    return u.getNickname() != null ? u.getNickname() : "A player";
                })
                .orElse("A retired player");
    }

    private String formatMatchSummary(Match match) {
        int gameCount = match.getGames() != null ? match.getGames().size() : 0;
        return gameCount + (gameCount == 1 ? " game submitted" : " games submitted");
    }

    private void dispatchPushNotification(PushSubscription sub, UUID recipientId, UUID matchId, String type, String jsonPayload) {
        dispatchPushNotification(sub, recipientId, matchId, null, type, jsonPayload);
    }

    private void dispatchPushNotification(PushSubscription sub, UUID recipientId, UUID matchId, UUID poolId, String type, String jsonPayload) {
        try {
            PushService pushService = getPushService();
            byte[] authBytes = safeDecodeBase64(sub.getAuth());
            byte[] p256dhBytes = safeDecodeBase64(sub.getP256dh());

            Notification notification = new Notification(
                    sub.getEndpoint(),
                    Utils.loadPublicKey(p256dhBytes),
                    authBytes,
                    jsonPayload.getBytes()
            );

            var response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                recordNotificationLog(recipientId, matchId, poolId, type, jsonPayload, "DELIVERED", null);
            } else if (statusCode == 404 || statusCode == 410) {
                log.info("Push subscription expired/invalid for recipient {}. Removing endpoint.", recipientId);
                notificationOperation.deleteSubscriptionByEndpoint(sub.getEndpoint());
                recordNotificationLog(recipientId, matchId, poolId, type, jsonPayload, "FAILED", "Expired subscription HTTP " + statusCode);
            } else {
                recordNotificationLog(recipientId, matchId, poolId, type, jsonPayload, "FAILED", "Push server returned HTTP " + statusCode);
            }
        } catch (Exception e) {
            log.warn("Failed to deliver Web Push to recipient {}: {}", recipientId, e.getMessage());
            recordNotificationLog(recipientId, matchId, poolId, type, jsonPayload, "FAILED", e.getMessage());
        }
    }

    private byte[] safeDecodeBase64(String value) {
        if (value == null || value.isBlank()) {
            return new byte[0];
        }
        String normalized = value.trim();
        try {
            return java.util.Base64.getUrlDecoder().decode(normalized);
        } catch (IllegalArgumentException e1) {
            try {
                return java.util.Base64.getDecoder().decode(normalized);
            } catch (IllegalArgumentException e2) {
                int missingPadding = (4 - (normalized.length() % 4)) % 4;
                String padded = normalized + "=".repeat(missingPadding);
                return java.util.Base64.getUrlDecoder().decode(padded);
            }
        }
    }

    private void recordNotificationLog(UUID recipientId, UUID matchId, String type, String payload, String status, String errorMessage) {
        recordNotificationLog(recipientId, matchId, null, type, payload, status, errorMessage);
    }

    private void recordNotificationLog(UUID recipientId, UUID matchId, UUID poolId, String type, String payload, String status, String errorMessage) {
        NotificationLog logEntry = NotificationLog.builder()
                .recipientId(recipientId)
                .matchId(matchId)
                .poolId(poolId)
                .type(type)
                .payload(payload)
                .status(status)
                .errorMessage(errorMessage)
                .sentAt(Instant.now())
                .build();
        try {
            notificationOperation.saveNotificationLog(logEntry);
        } catch (Exception e) {
            log.error("Failed to save NotificationLog entry for recipient {}", recipientId, e);
        }
    }
}
