package com.tictactore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.VapidProperties;
import com.tictactore.dto.NotificationLogDto;
import com.tictactore.dto.PushNotificationPayload;
import com.tictactore.dto.PushSubscriptionRequest;
import com.tictactore.model.Match;
import com.tictactore.model.MatchType;
import com.tictactore.model.NotificationLog;
import com.tictactore.model.PushSubscription;
import com.tictactore.model.SkillLevel;
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
        var payload = new PushNotificationPayload(match.getId(), creatorName, summary, isDuplicateWarning, Instant.now().toString());
        for (User opponent : opponents) {
            sendPushToUser(opponent.getId(), match.getId(), null, null, "CONFIRMATION_REQUEST", payload);
        }
    }

    @Override
    public void sendRejectionNotification(Match match, User creator, String rejectionReason) {
        if (creator == null || creator.getId() == null) return;
        String opponentName = resolveCreatorName(match.getRejectedByUserId());
        String summary = opponentName + " rejected your match. Reason: " + rejectionReason;
        var payload = new PushNotificationPayload(match.getId(), opponentName, summary, false, Instant.now().toString());
        sendPushToUser(creator.getId(), match.getId(), null, null, "MATCH_REJECTED", payload);
    }

    @Override
    public void sendPartialConfirmationNotification(Match match, List<User> remainingOpponents, String firstConfirmerName) {
        String summary = firstConfirmerName + " confirmed your match. Waiting for " + remainingOpponents.size()
                + " more opponent" + (remainingOpponents.size() == 1 ? "" : "s") + " to confirm.";
        var payload = new PushNotificationPayload(match.getId(), firstConfirmerName, summary, false, Instant.now().toString());
        for (User opponent : remainingOpponents) {
            sendPushToUser(opponent.getId(), match.getId(), null, null, "PARTIAL_CONFIRMATION", payload);
        }
    }

    @Override
    public void sendCooldownReminderNotification(Match match, List<User> recipients) {
        String summary = "Match " + match.getId() + " is awaiting final confirmation. It will be auto-published when the cooldown expires.";
        var payload = new PushNotificationPayload(match.getId(), "System", summary, false, Instant.now().toString());
        for (User recipient : recipients) {
            sendPushToUser(recipient.getId(), match.getId(), null, null, "COOLDOWN_REMINDER", payload);
        }
    }

    @Override
    public void sendPoolCreatedNotification(UUID poolId, UUID creatorId, String creatorName, MatchType matchType, SkillLevel skillLevel, List<User> recipients) {
        if (recipients == null || recipients.isEmpty()) return;
        String resolvedCreator = resolveCreatorName(creatorId);
        String matchTypeStr = matchType == MatchType.TWO_VS_TWO ? "2v2" : "1v1";
        var payload = new PushNotificationPayload(null, poolId, "POOL_CREATED", resolvedCreator, "A new " + matchTypeStr + " pool is looking for players", "/", false, Instant.now().toString());
        for (User recipient : recipients) {
            sendPushToUser(recipient.getId(), null, poolId, null, "POOL_CREATED", payload);
        }
    }

    @Override
    public void sendPoolFilledNotification(UUID poolId, MatchType matchType, List<User> participants) {
        if (participants == null || participants.isEmpty()) return;
        String matchTypeStr = matchType == MatchType.TWO_VS_TWO ? "2v2" : "1v1";
        var payload = new PushNotificationPayload(null, poolId, "POOL_FILLED", "System", "Your " + matchTypeStr + " pool is full — head to the table!", "/", false, Instant.now().toString());
        for (User participant : participants) {
            sendPushToUser(participant.getId(), null, poolId, null, "POOL_FILLED", payload);
        }
    }

    @Override
    public void sendChallengeCreatedNotification(UUID challengeId, String challengerName, MatchType matchType, List<User> recipients) {
        if (recipients == null || recipients.isEmpty()) return;
        String matchTypeStr = matchType == MatchType.TWO_VS_TWO ? "2v2" : "1v1";
        var payload = new PushNotificationPayload(null, null, challengeId, "CHALLENGE_RECEIVED", challengerName, challengerName + " challenged you to a " + matchTypeStr + " match", "/?tab=challenges", false, Instant.now().toString());
        for (User recipient : recipients) {
            sendPushToUser(recipient.getId(), null, null, challengeId, "CHALLENGE_RECEIVED", payload);
        }
    }

    @Override
    public void sendChallengeAcceptedNotification(UUID challengeId, String targetName, MatchType matchType, User challenger) {
        if (challenger == null || challenger.getId() == null) return;
        var payload = new PushNotificationPayload(null, null, challengeId, "CHALLENGE_ACCEPTED", targetName, targetName + " accepted your challenge — head to the table!", "/?challengeId=" + challengeId, false, Instant.now().toString());
        sendPushToUser(challenger.getId(), null, null, challengeId, "CHALLENGE_ACCEPTED", payload);
    }

    @Override
    public void sendChallengeDeclinedNotification(UUID challengeId, String targetName, User challenger) {
        if (challenger == null || challenger.getId() == null) return;
        var payload = new PushNotificationPayload(null, null, challengeId, "CHALLENGE_DECLINED", targetName, targetName + " declined your challenge.", "/?tab=challenges", false, Instant.now().toString());
        sendPushToUser(challenger.getId(), null, null, challengeId, "CHALLENGE_DECLINED", payload);
    }

    @Override
    public void sendTournamentInviteNotification(UUID tournamentId, String tournamentName, String inviterName, User recipient) {
        sendTournamentPush(recipient, tournamentId, "TOURNAMENT_INVITE", inviterName + " invited you to team up for " + tournamentName, inviterName);
    }

    @Override
    public void sendTournamentInviteAcceptedNotification(UUID tournamentId, String tournamentName, String partnerName, User recipient) {
        sendTournamentPush(recipient, tournamentId, "TOURNAMENT_INVITE_ACCEPTED", partnerName + " accepted your team invitation for " + tournamentName + "!", partnerName);
    }

    @Override
    public void sendTournamentInviteDeclinedNotification(UUID tournamentId, String tournamentName, String partnerName, User recipient) {
        sendTournamentPush(recipient, tournamentId, "TOURNAMENT_INVITE_DECLINED", partnerName + " declined your team invitation for " + tournamentName + ".", partnerName);
    }

    @Override
    public void sendTournamentRegistrationCancelledNotification(UUID tournamentId, String tournamentName, String cancellerName, User recipient) {
        sendTournamentPush(recipient, tournamentId, "TOURNAMENT_REGISTRATION_CANCELLED", cancellerName + " cancelled the team registration for " + tournamentName + ".", cancellerName);
    }

    @Override
    public void sendTournamentStartedNotification(UUID tournamentId, String tournamentName, User recipient) {
        sendTournamentPush(recipient, tournamentId, "TOURNAMENT_STARTED", tournamentName + " has started! Your initial matches are ready.", "System");
    }

    @Override
    public void sendTournamentCancelledNotification(UUID tournamentId, String tournamentName, String reason, User recipient) {
        sendTournamentPush(recipient, tournamentId, "TOURNAMENT_CANCELLED", tournamentName + " has been cancelled. Reason: " + reason, "System");
    }

    @Override
    public void sendTournamentStubPartnerAssignedNotification(UUID tournamentId, String tournamentName, UUID matchId, User recipient, boolean isStub) {
        if (recipient == null || recipient.getId() == null) return;
        String summary = isStub
                ? "You have been assigned as a substitute partner in " + tournamentName + "."
                : "A substitute partner has been assigned to your match in " + tournamentName + ".";
        var payload = PushNotificationPayload.builder()
                .tournamentId(tournamentId)
                .type("TOURNAMENT_STUB_PARTNER_ASSIGNED")
                .creatorName("System")
                .summary(summary)
                .url("/tournaments?tournamentId=" + tournamentId)
                .isDuplicateWarning(false)
                .timestamp(Instant.now().toString())
                .build();
        sendPushToUser(recipient.getId(), matchId, null, null, "TOURNAMENT_STUB_PARTNER_ASSIGNED", payload);
    }

    private void sendTournamentPush(User recipient, UUID tournamentId, String type, String summary, String creator) {
        if (recipient == null || recipient.getId() == null) return;
        var payload = PushNotificationPayload.builder()
                .tournamentId(tournamentId)
                .type(type)
                .creatorName(creator)
                .summary(summary)
                .url("/tournaments?tournamentId=" + tournamentId)
                .isDuplicateWarning(false)
                .timestamp(Instant.now().toString())
                .build();
        sendPushToUser(recipient.getId(), null, null, null, type, payload);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationLogDto> getUserNotifications(UUID userId) {
        if (userId == null) return List.of();
        return notificationOperation.getNotificationsForUser(userId).stream()
                .map(logEntry -> new NotificationLogDto(
                        logEntry.getId(), logEntry.getRecipientId(), logEntry.getMatchId(), logEntry.getPoolId(),
                        logEntry.getChallengeId(), logEntry.getType(), logEntry.getPayload(),
                        logEntry.getStatus(), logEntry.getErrorMessage(), logEntry.getSentAt()
                ))
                .toList();
    }

    private void sendPushToUser(UUID recipientId, UUID matchId, UUID poolId, UUID challengeId, String type, Object payloadDto) {
        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadDto);
        } catch (Exception e) {
            log.error("Failed to serialize push notification payload for type {} and recipient {}", type, recipientId, e);
            return;
        }

        List<PushSubscription> subscriptions = notificationOperation.getSubscriptionsForUser(recipientId);
        if (subscriptions.isEmpty()) {
            recordNotificationLog(recipientId, matchId, poolId, challengeId, type, jsonPayload, "SKIPPED", "No push subscription registered");
            return;
        }

        for (PushSubscription sub : subscriptions) {
            dispatchPushNotification(sub, recipientId, matchId, poolId, challengeId, type, jsonPayload);
        }
    }

    private String resolveCreatorName(UUID creatorId) {
        if (creatorId == null) return "A retired player";
        return userRepository.findById(creatorId)
                .map(u -> (u.getNickname() != null && u.getNickname().startsWith("ex-player-")) ? "A retired player" : (u.getNickname() != null ? u.getNickname() : "A player"))
                .orElse("A retired player");
    }

    private String formatMatchSummary(Match match) {
        int gameCount = match.getGames() != null ? match.getGames().size() : 0;
        return gameCount + (gameCount == 1 ? " game submitted" : " games submitted");
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
        if (value == null || value.isBlank()) return new byte[0];
        String normalized = value.trim();
        try {
            return java.util.Base64.getUrlDecoder().decode(normalized);
        } catch (IllegalArgumentException e1) {
            try {
                return java.util.Base64.getDecoder().decode(normalized);
            } catch (IllegalArgumentException e2) {
                int missingPadding = (4 - (normalized.length() % 4)) % 4;
                return java.util.Base64.getUrlDecoder().decode(normalized + "=".repeat(missingPadding));
            }
        }
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
