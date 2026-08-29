package com.tictactore.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for immutable notification audit log records.
 *
 * @param id unique log identifier
 * @param recipientId user ID of the recipient
 * @param matchId match ID associated with the notification
 * @param poolId pool ID associated with the notification
 * @param type type of notification
 * @param payload raw JSON payload sent
 * @param status delivery status (DELIVERED, QUEUED, FAILED)
 * @param errorMessage error message if delivery failed
 * @param sentAt timestamp when notification was processed
 */
public record NotificationLogDto(
    UUID id,
    UUID recipientId,
    UUID matchId,
    UUID poolId,
    String type,
    String payload,
    String status,
    String errorMessage,
    Instant sentAt
) {
    public NotificationLogDto(
        UUID id,
        UUID recipientId,
        UUID matchId,
        String type,
        String payload,
        String status,
        String errorMessage,
        Instant sentAt
    ) {
        this(id, recipientId, matchId, null, type, payload, status, errorMessage, sentAt);
    }
}
