package com.tictactore.dto;

import java.util.Map;
import java.util.UUID;

public record PlayerInsightDto(
        UUID id,
        InsightType type,
        InsightCategory category,
        InsightImportance importance,
        String titleKey,
        String descriptionKey,
        Map<String, Object> params,
        String icon,
        String drillDownUrl
) {
}
