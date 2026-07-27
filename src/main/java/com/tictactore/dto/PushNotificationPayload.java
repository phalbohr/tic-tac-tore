package com.tictactore.dto;

import java.util.UUID;

/**
 * Payload JSON structure dispatched in Web Push notifications.
 *
 * @param matchId the unique identifier of the match awaiting confirmation
 * @param creatorName the display name of the creator (or "A retired player" if pseudonymized)
 * @param summary summary description of the match score / outcome
 * @param isDuplicateWarning flag indicating whether a duplicate match on the same UTC day was detected
 * @param timestamp ISO-8601 formatted timestamp of push creation
 */
public record PushNotificationPayload(
    UUID matchId,
    String creatorName,
    String summary,
    boolean isDuplicateWarning,
    String timestamp
) {}
