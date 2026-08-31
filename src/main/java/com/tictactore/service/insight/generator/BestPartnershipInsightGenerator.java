package com.tictactore.service.insight.generator;

import com.tictactore.dto.AchievementDto;
import com.tictactore.dto.InsightCategory;
import com.tictactore.dto.InsightImportance;
import com.tictactore.dto.InsightType;
import com.tictactore.dto.PlayerInsightDto;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.achievement.PlayerStatsContext;
import com.tictactore.service.insight.InsightGenerator;
import com.tictactore.service.insight.InsightMatchUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BestPartnershipInsightGenerator implements InsightGenerator {

    private final UserRepository userRepository;

    public BestPartnershipInsightGenerator() {
        this.userRepository = null;
    }

    @Override
    public Optional<PlayerInsightDto> generate(
            UUID playerId,
            List<Match> matches,
            PlayerStatsContext stats,
            List<AchievementDto> achievements
    ) {
        if (matches == null || matches.isEmpty()) {
            return Optional.empty();
        }

        Map<UUID, PartnerStats> partnerStatsMap = new HashMap<>();

        for (Match match : matches) {
            if (match.getTeamADefenderId() == null || match.getTeamBDefenderId() == null) {
                continue;
            }

            boolean inTeamA = playerId.equals(match.getTeamAAttackerId()) || playerId.equals(match.getTeamADefenderId());
            boolean inTeamB = playerId.equals(match.getTeamBAttackerId()) || playerId.equals(match.getTeamBDefenderId());

            if (!inTeamA && !inTeamB) {
                continue;
            }

            UUID partnerId = inTeamA
                    ? (playerId.equals(match.getTeamAAttackerId()) ? match.getTeamADefenderId() : match.getTeamAAttackerId())
                    : (playerId.equals(match.getTeamBAttackerId()) ? match.getTeamBDefenderId() : match.getTeamBAttackerId());

            if (partnerId == null) {
                continue;
            }

            boolean won = InsightMatchUtils.isPlayerWinner(match, playerId);
            PartnerStats partnerStats = partnerStatsMap.computeIfAbsent(partnerId, k -> new PartnerStats());
            partnerStats.matches++;
            if (won) {
                partnerStats.wins++;
            }
        }

        UUID bestPartnerId = null;
        double bestWinRate = -1.0;
        int bestMatches = 0;

        for (Map.Entry<UUID, PartnerStats> entry : partnerStatsMap.entrySet()) {
            PartnerStats ps = entry.getValue();
            if (ps.matches >= 3) {
                double winRate = ((double) ps.wins / ps.matches) * 100.0;
                if (winRate >= 70.0) {
                    if (winRate > bestWinRate || (Double.compare(winRate, bestWinRate) == 0 && ps.matches > bestMatches)) {
                        bestPartnerId = entry.getKey();
                        bestWinRate = winRate;
                        bestMatches = ps.matches;
                    }
                }
            }
        }

        if (bestPartnerId == null) {
            return Optional.empty();
        }

        String partnerName = "Partner";
        if (userRepository != null) {
            partnerName = userRepository.findById(bestPartnerId)
                    .map(User::getNickname)
                    .filter(name -> name != null && !name.isBlank())
                    .orElse("Partner");
        }

        return Optional.of(new PlayerInsightDto(
                UUID.randomUUID(),
                InsightType.BEST_PARTNERSHIP,
                InsightCategory.PARTNERSHIP,
                InsightImportance.MEDIUM,
                "insights.bestPartnership.title",
                "insights.bestPartnership.description",
                Map.of(
                        "partnerId", bestPartnerId,
                        "partnerName", partnerName,
                        "winRate", Math.round(bestWinRate),
                        "matches", bestMatches
                ),
                "group",
                "/statistics?tab=teams"
        ));
    }

    @Override
    public int getOrder() {
        return 40;
    }

    private static class PartnerStats {
        int matches = 0;
        int wins = 0;
    }
}
