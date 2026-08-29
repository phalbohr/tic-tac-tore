package com.tictactore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

/**
 * Payload JSON structure dispatched in Web Push notifications.
 *
 * @param matchId the unique identifier of the match awaiting confirmation
 * @param poolId the unique identifier of the matchmaking pool
 * @param type the notification event type (e.g. POOL_CREATED, POOL_FILLED, CONFIRMATION_REQUEST)
 * @param creatorName the display name of the creator (or "A retired player" if pseudonymized)
 * @param summary summary description of the match/pool event
 * @param url target deep link URL
 * @param isDuplicateWarning flag indicating whether a duplicate match was detected
 * @param timestamp ISO-8601 formatted timestamp of push creation
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PushNotificationPayload(
    UUID matchId,
    UUID poolId,
    String type,
    String creatorName,
    String summary,
    String url,
    boolean isDuplicateWarning,
    String timestamp
) {
    public PushNotificationPayload(
        UUID matchId,
        String creatorName,
        String summary,
        boolean isDuplicateWarning,
        String timestamp
    ) {
        this(matchId, null, null, creatorName, summary, null, isDuplicateWarning, timestamp);
    }
}
