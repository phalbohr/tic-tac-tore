package com.tictactore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

/**
 * Payload JSON structure dispatched in Web Push notifications.
 *
 * @param matchId the unique identifier of the match awaiting confirmation
 * @param poolId the unique identifier of the matchmaking pool
 * @param challengeId the unique identifier of the match challenge
 * @param tournamentId the unique identifier of the tournament
 * @param type the notification event type (e.g. POOL_CREATED, POOL_FILLED, CONFIRMATION_REQUEST, CHALLENGE_RECEIVED, CHALLENGE_ACCEPTED, CHALLENGE_DECLINED, TOURNAMENT_INVITE, TOURNAMENT_INVITE_ACCEPTED, TOURNAMENT_INVITE_DECLINED)
 * @param creatorName the display name of the creator (or "A retired player" if pseudonymized)
 * @param summary summary description of the match/pool/challenge/tournament event
 * @param url target deep link URL
 * @param isDuplicateWarning flag indicating whether a duplicate match was detected
 * @param timestamp ISO-8601 formatted timestamp of push creation
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PushNotificationPayload(
    UUID matchId,
    UUID poolId,
    UUID challengeId,
    UUID tournamentId,
    String type,
    String creatorName,
    String summary,
    String url,
    boolean isDuplicateWarning,
    String timestamp
) {
    public PushNotificationPayload(
        UUID matchId,
        UUID poolId,
        UUID challengeId,
        String type,
        String creatorName,
        String summary,
        String url,
        boolean isDuplicateWarning,
        String timestamp
    ) {
        this(matchId, poolId, challengeId, null, type, creatorName, summary, url, isDuplicateWarning, timestamp);
    }

    public PushNotificationPayload(
        UUID matchId,
        UUID poolId,
        String type,
        String creatorName,
        String summary,
        String url,
        boolean isDuplicateWarning,
        String timestamp
    ) {
        this(matchId, poolId, null, null, type, creatorName, summary, url, isDuplicateWarning, timestamp);
    }

    public PushNotificationPayload(
        UUID matchId,
        String creatorName,
        String summary,
        boolean isDuplicateWarning,
        String timestamp
    ) {
        this(matchId, null, null, null, null, creatorName, summary, null, isDuplicateWarning, timestamp);
    }
}
