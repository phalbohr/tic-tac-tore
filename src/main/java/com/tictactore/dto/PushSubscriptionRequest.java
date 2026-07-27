package com.tictactore.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for subscribing to Web Push notifications.
 *
 * @param endpoint the push service subscription URL endpoint
 * @param p256dh the client public key string (P-256 curve)
 * @param auth the client authentication secret string
 */
public record PushSubscriptionRequest(
    @NotBlank String endpoint,
    @NotBlank String p256dh,
    @NotBlank String auth
) {}
