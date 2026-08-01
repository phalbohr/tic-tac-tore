package com.tictactore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MatchRejectionRequest(
    @NotBlank(message = "Rejection reason is required")
    String reason,

    @Size(max = 200, message = "Custom reason must not exceed 200 characters")
    String customReason
) {
}
