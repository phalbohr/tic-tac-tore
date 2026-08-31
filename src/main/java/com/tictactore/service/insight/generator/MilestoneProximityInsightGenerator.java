package com.tictactore.service.insight.generator;

import com.tictactore.dto.AchievementDto;
import com.tictactore.dto.InsightCategory;
import com.tictactore.dto.InsightImportance;
import com.tictactore.dto.InsightType;
import com.tictactore.dto.PlayerInsightDto;
import com.tictactore.model.Match;
import com.tictactore.service.achievement.PlayerStatsContext;
import com.tictactore.service.insight.InsightGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class MilestoneProximityInsightGenerator implements InsightGenerator {

    @Override
    public Optional<PlayerInsightDto> generate(
            UUID playerId,
            List<Match> matches,
            PlayerStatsContext stats,
            List<AchievementDto> achievements
    ) {
        if (achievements == null || achievements.isEmpty()) {
            return Optional.empty();
        }

        AchievementDto closestBadge = null;
        long minRemaining = Long.MAX_VALUE;

        for (AchievementDto badge : achievements) {
            if (!badge.isUnlocked() && badge.hasProgress() && badge.currentProgress() != null && badge.targetValue() != null) {
                long remaining = badge.targetValue() - badge.currentProgress();
                if (remaining > 0 && remaining <= 2) {
                    if (remaining < minRemaining) {
                        minRemaining = remaining;
                        closestBadge = badge;
                    }
                }
            }
        }

        if (closestBadge == null) {
            return Optional.empty();
        }

        return Optional.of(new PlayerInsightDto(
                UUID.randomUUID(),
                InsightType.MILESTONE_PROXIMITY,
                InsightCategory.MILESTONE,
                InsightImportance.MEDIUM,
                "insights.milestoneProximity.title",
                "insights.milestoneProximity.description",
                Map.of(
                        "badgeCode", closestBadge.code(),
                        "remaining", minRemaining,
                        "current", closestBadge.currentProgress(),
                        "target", closestBadge.targetValue()
                ),
                "military_tech",
                "/cabinet"
        ));
    }

    @Override
    public int getOrder() {
        return 50;
    }
}
