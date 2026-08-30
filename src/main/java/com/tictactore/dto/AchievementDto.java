package com.tictactore.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AchievementDto(
        UUID id,
        String code,
        String category,
        String nameKey,
        String descriptionKey,
        String icon,
        boolean isUnlocked,
        OffsetDateTime unlockedAt,
        Long currentProgress,
        Long targetValue,
        boolean hasProgress
) {
}
