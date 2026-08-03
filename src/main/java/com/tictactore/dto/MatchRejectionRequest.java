package com.tictactore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MatchRejectionRequest(
    @NotBlank(message = "Rejection reason is required")
    @Size(max = 100, message = "Rejection reason must not exceed 100 characters")
    String reason,

    @Size(max = 200, message = "Custom reason must not exceed 200 characters")
    String customReason
) {
}
