package com.tictactore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.VapidProperties;
import com.tictactore.dto.NotificationLogDto;
import com.tictactore.dto.PushNotificationPayload;
import com.tictactore.dto.PushSubscriptionRequest;
import com.tictactore.model.NotificationLog;
import com.tictactore.model.Match;
import com.tictactore.model.MatchType;
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
    public void sendChallengeCreatedNotification(
            UUID challengeId,
            String challengerName,
            MatchType matchType,
            List<User> recipients
    ) {
        if (recipients == null || recipients.isEmpty()) {
            return;
        }

        String matchTypeStr = matchType == MatchType.TWO_VS_TWO ? "2v2" : "1v1";
        String summary = challengerName + " challenged you to a " + matchTypeStr + " match";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = new PushNotificationPayload(
                null,
                null,
                challengeId,
                "CHALLENGE_RECEIVED",
                challengerName,
                summary,
                "/?tab=challenges",
                false,
                timestamp
        );

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize challenge created push notification payload for challenge {}", challengeId, e);
            return;
        }

        for (User recipient : recipients) {
            List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(recipient.getId());
            if (subscriptions.isEmpty()) {
                recordNotificationLog(recipient.getId(), null, null, challengeId, "CHALLENGE_RECEIVED", jsonPayload, "SKIPPED", "No push subscription registered");
                continue;
            }

            for (PushSubscription sub : subscriptions) {
                dispatchPushNotification(sub, recipient.getId(), null, null, challengeId, "CHALLENGE_RECEIVED", jsonPayload);
            }
        }
    }

    @Override
    public void sendChallengeAcceptedNotification(
            UUID challengeId,
            String targetName,
            MatchType matchType,
            User challenger
    ) {
        if (challenger == null || challenger.getId() == null) {
            return;
        }

        String summary = targetName + " accepted your challenge — head to the table!";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = new PushNotificationPayload(
                null,
                null,
                challengeId,
                "CHALLENGE_ACCEPTED",
                targetName,
                summary,
                "/?challengeId=" + challengeId,
                false,
                timestamp
        );

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize challenge accepted push notification payload for challenge {}", challengeId, e);
            return;
        }

        List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(challenger.getId());
        if (subscriptions.isEmpty()) {
            recordNotificationLog(challenger.getId(), null, null, challengeId, "CHALLENGE_ACCEPTED", jsonPayload, "SKIPPED", "No push subscription registered");
            return;
        }

        for (PushSubscription sub : subscriptions) {
            dispatchPushNotification(sub, challenger.getId(), null, null, challengeId, "CHALLENGE_ACCEPTED", jsonPayload);
        }
    }

    @Override
    public void sendChallengeDeclinedNotification(
            UUID challengeId,
            String targetName,
            User challenger
    ) {
        if (challenger == null || challenger.getId() == null) {
            return;
        }

        String summary = targetName + " declined your challenge.";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = new PushNotificationPayload(
                null,
                null,
                challengeId,
                "CHALLENGE_DECLINED",
                targetName,
                summary,
                "/?tab=challenges",
                false,
                timestamp
        );

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize challenge declined push notification payload for challenge {}", challengeId, e);
            return;
        }

        List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(challenger.getId());
        if (subscriptions.isEmpty()) {
            recordNotificationLog(challenger.getId(), null, null, challengeId, "CHALLENGE_DECLINED", jsonPayload, "SKIPPED", "No push subscription registered");
            return;
        }

        for (PushSubscription sub : subscriptions) {
            dispatchPushNotification(sub, challenger.getId(), null, null, challengeId, "CHALLENGE_DECLINED", jsonPayload);
        }
    }

    @Override
    public void sendTournamentInviteNotification(
            UUID tournamentId,
            String tournamentName,
            String inviterName,
            User recipient
    ) {
        if (recipient == null || recipient.getId() == null) {
            return;
        }

        String summary = inviterName + " invited you to team up for " + tournamentName;
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = PushNotificationPayload.builder()
                .tournamentId(tournamentId)
                .type("TOURNAMENT_INVITE")
                .creatorName(inviterName)
                .summary(summary)
                .url("/tournaments?tournamentId=" + tournamentId)
                .isDuplicateWarning(false)
                .timestamp(timestamp)
                .build();

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize tournament invite push notification payload for tournament {}", tournamentId, e);
            return;
        }

        List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(recipient.getId());
        if (subscriptions.isEmpty()) {
            recordNotificationLog(recipient.getId(), null, null, null, "TOURNAMENT_INVITE", jsonPayload, "SKIPPED", "No push subscription registered");
            return;
        }

        for (PushSubscription sub : subscriptions) {
            dispatchPushNotification(sub, recipient.getId(), null, null, null, "TOURNAMENT_INVITE", jsonPayload);
        }
    }

    @Override
    public void sendTournamentInviteAcceptedNotification(
            UUID tournamentId,
            String tournamentName,
            String partnerName,
            User recipient
    ) {
        if (recipient == null || recipient.getId() == null) {
            return;
        }

        String summary = partnerName + " accepted your team invitation for " + tournamentName + "!";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = PushNotificationPayload.builder()
                .tournamentId(tournamentId)
                .type("TOURNAMENT_INVITE_ACCEPTED")
                .creatorName(partnerName)
                .summary(summary)
                .url("/tournaments?tournamentId=" + tournamentId)
                .isDuplicateWarning(false)
                .timestamp(timestamp)
                .build();

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize tournament invite accepted push notification payload for tournament {}", tournamentId, e);
            return;
        }

        List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(recipient.getId());
        if (subscriptions.isEmpty()) {
            recordNotificationLog(recipient.getId(), null, null, null, "TOURNAMENT_INVITE_ACCEPTED", jsonPayload, "SKIPPED", "No push subscription registered");
            return;
        }

        for (PushSubscription sub : subscriptions) {
            dispatchPushNotification(sub, recipient.getId(), null, null, null, "TOURNAMENT_INVITE_ACCEPTED", jsonPayload);
        }
    }

    @Override
    public void sendTournamentInviteDeclinedNotification(
            UUID tournamentId,
            String tournamentName,
            String partnerName,
            User recipient
    ) {
        if (recipient == null || recipient.getId() == null) {
            return;
        }

        String summary = partnerName + " declined your team invitation for " + tournamentName + ".";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = PushNotificationPayload.builder()
                .tournamentId(tournamentId)
                .type("TOURNAMENT_INVITE_DECLINED")
                .creatorName(partnerName)
                .summary(summary)
                .url("/tournaments?tournamentId=" + tournamentId)
                .isDuplicateWarning(false)
                .timestamp(timestamp)
                .build();

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize tournament invite declined push notification payload for tournament {}", tournamentId, e);
            return;
        }

        List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(recipient.getId());
        if (subscriptions.isEmpty()) {
            recordNotificationLog(recipient.getId(), null, null, null, "TOURNAMENT_INVITE_DECLINED", jsonPayload, "SKIPPED", "No push subscription registered");
            return;
        }

        for (PushSubscription sub : subscriptions) {
            dispatchPushNotification(sub, recipient.getId(), null, null, null, "TOURNAMENT_INVITE_DECLINED", jsonPayload);
        }
    }

    @Override
    public void sendTournamentRegistrationCancelledNotification(
            UUID tournamentId,
            String tournamentName,
            String cancellerName,
            User recipient
    ) {
        if (recipient == null || recipient.getId() == null) {
            return;
        }

        String summary = cancellerName + " cancelled the team registration for " + tournamentName + ".";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = PushNotificationPayload.builder()
                .tournamentId(tournamentId)
                .type("TOURNAMENT_REGISTRATION_CANCELLED")
                .creatorName(cancellerName)
                .summary(summary)
                .url("/tournaments?tournamentId=" + tournamentId)
                .isDuplicateWarning(false)
                .timestamp(timestamp)
                .build();

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize tournament registration cancelled push notification payload for tournament {}", tournamentId, e);
            return;
        }

        List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(recipient.getId());
        if (subscriptions.isEmpty()) {
            recordNotificationLog(recipient.getId(), null, null, null, "TOURNAMENT_REGISTRATION_CANCELLED", jsonPayload, "SKIPPED", "No push subscription registered");
            return;
        }

        for (PushSubscription sub : subscriptions) {
            dispatchPushNotification(sub, recipient.getId(), null, null, null, "TOURNAMENT_REGISTRATION_CANCELLED", jsonPayload);
        }
    }

    @Override
    public void sendTournamentStartedNotification(
            UUID tournamentId,
            String tournamentName,
            User recipient
    ) {
        if (recipient == null || recipient.getId() == null) {
            return;
        }

        String summary = tournamentName + " has started! Your initial matches are ready.";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = PushNotificationPayload.builder()
                .tournamentId(tournamentId)
                .type("TOURNAMENT_STARTED")
                .creatorName("System")
                .summary(summary)
                .url("/tournaments?tournamentId=" + tournamentId)
                .isDuplicateWarning(false)
                .timestamp(timestamp)
                .build();

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize tournament started push notification payload for tournament {}", tournamentId, e);
            return;
        }

        List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(recipient.getId());
        if (subscriptions.isEmpty()) {
            recordNotificationLog(recipient.getId(), null, null, null, "TOURNAMENT_STARTED", jsonPayload, "SKIPPED", "No push subscription registered");
            return;
        }

        for (PushSubscription sub : subscriptions) {
            dispatchPushNotification(sub, recipient.getId(), null, null, null, "TOURNAMENT_STARTED", jsonPayload);
        }
    }

    @Override
    public void sendTournamentCancelledNotification(
            UUID tournamentId,
            String tournamentName,
            String reason,
            User recipient
    ) {
        if (recipient == null || recipient.getId() == null) {
            return;
        }

        String summary = tournamentName + " has been cancelled. Reason: " + reason;
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = PushNotificationPayload.builder()
                .tournamentId(tournamentId)
                .type("TOURNAMENT_CANCELLED")
                .creatorName("System")
                .summary(summary)
                .url("/tournaments?tournamentId=" + tournamentId)
                .isDuplicateWarning(false)
                .timestamp(timestamp)
                .build();

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize tournament cancelled push notification payload for tournament {}", tournamentId, e);
            return;
        }

        List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(recipient.getId());
        if (subscriptions.isEmpty()) {
            recordNotificationLog(recipient.getId(), null, null, null, "TOURNAMENT_CANCELLED", jsonPayload, "SKIPPED", "No push subscription registered");
            return;
        }

        for (PushSubscription sub : subscriptions) {
            dispatchPushNotification(sub, recipient.getId(), null, null, null, "TOURNAMENT_CANCELLED", jsonPayload);
        }
    }

    @Override
    public void sendTournamentStubPartnerAssignedNotification(
            UUID tournamentId,
            String tournamentName,
            UUID matchId,
            User recipient,
            boolean isStub
    ) {
        if (recipient == null || recipient.getId() == null) {
            return;
        }

        String summary = isStub
                ? "You have been assigned as a substitute partner in " + tournamentName + "."
                : "A substitute partner has been assigned to your match in " + tournamentName + ".";
        String timestamp = Instant.now().toString();

        PushNotificationPayload payloadDto = PushNotificationPayload.builder()
                .tournamentId(tournamentId)
                .type("TOURNAMENT_STUB_PARTNER_ASSIGNED")
                .creatorName("System")
                .summary(summary)
                .url("/tournaments?tournamentId=" + tournamentId)
                .isDuplicateWarning(false)
                .timestamp(timestamp)
                .build();

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize tournament stub partner push notification for tournament {}", tournamentId, e);
            return;
        }

        List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(recipient.getId());
        if (subscriptions.isEmpty()) {
            recordNotificationLog(recipient.getId(), null, null, null, "TOURNAMENT_STUB_PARTNER_ASSIGNED", jsonPayload, "SKIPPED", "No push subscription registered");
            return;
        }

        for (PushSubscription sub : subscriptions) {
            dispatchPushNotification(sub, recipient.getId(), null, null, null, "TOURNAMENT_STUB_PARTNER_ASSIGNED", jsonPayload);
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
                        logEntry.getChallengeId(),
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
        dispatchPushNotification(sub, recipientId, matchId, null, null, type, jsonPayload);
    }

    private void dispatchPushNotification(PushSubscription sub, UUID recipientId, UUID matchId, UUID poolId, String type, String jsonPayload) {
        dispatchPushNotification(sub, recipientId, matchId, poolId, null, type, jsonPayload);
    }

    private void dispatchPushNotification(PushSubscription sub, UUID recipientId, UUID matchId, UUID poolId, UUID challengeId, String type, String jsonPayload) {
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
                recordNotificationLog(recipientId, matchId, poolId, challengeId, type, jsonPayload, "DELIVERED", null);
            } else if (statusCode == 404 || statusCode == 410) {
                log.info("Push subscription expired/invalid for recipient {}. Removing endpoint.", recipientId);
                notificationOperation.deleteSubscriptionByEndpoint(sub.getEndpoint());
                recordNotificationLog(recipientId, matchId, poolId, challengeId, type, jsonPayload, "FAILED", "Expired subscription HTTP " + statusCode);
            } else {
                recordNotificationLog(recipientId, matchId, poolId, challengeId, type, jsonPayload, "FAILED", "Push server returned HTTP " + statusCode);
            }
        } catch (Exception e) {
            log.warn("Failed to deliver Web Push to recipient {}: {}", recipientId, e.getMessage());
            recordNotificationLog(recipientId, matchId, poolId, challengeId, type, jsonPayload, "FAILED", e.getMessage());
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
        recordNotificationLog(recipientId, matchId, null, null, type, payload, status, errorMessage);
    }

    private void recordNotificationLog(UUID recipientId, UUID matchId, UUID poolId, String type, String payload, String status, String errorMessage) {
        recordNotificationLog(recipientId, matchId, poolId, null, type, payload, status, errorMessage);
    }

    private void recordNotificationLog(UUID recipientId, UUID matchId, UUID poolId, UUID challengeId, String type, String payload, String status, String errorMessage) {
        NotificationLog logEntry = NotificationLog.builder()
                .recipientId(recipientId)
                .matchId(matchId)
                .poolId(poolId)
                .challengeId(challengeId)
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
