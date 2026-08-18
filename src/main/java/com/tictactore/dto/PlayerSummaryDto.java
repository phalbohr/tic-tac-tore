package com.tictactore.dto;

import java.util.UUID;

public record PlayerSummaryDto(
        UUID id,
        String nickname,
        String avatarUrl
) {}
