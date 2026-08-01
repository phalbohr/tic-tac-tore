package com.tictactore.dto;

public record MatchConfirmationRequest(
    String idempotencyKey
) {}
