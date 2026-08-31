package com.tictactore.service.impl;

import com.tictactore.dto.AchievementDto;
import com.tictactore.dto.InsightCategory;
import com.tictactore.dto.InsightImportance;
import com.tictactore.dto.InsightType;
import com.tictactore.dto.PlayerInsightDto;
import com.tictactore.dto.PlayerInsightsResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.Match;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.AchievementService;
import com.tictactore.service.InsightService;
import com.tictactore.service.achievement.PlayerStatsContext;
import com.tictactore.service.insight.InsightGenerator;
import com.tictactore.service.insight.InsightMatchUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {

    private final MatchRepository matchRepository;
    private final AchievementService achievementService;
    private final List<InsightGenerator> generators;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PlayerInsightsResponse getPlayerInsights(UUID playerId) {
        if (playerId == null) {
            throw new ResourceNotFoundException("Player ID must not be null");
        }

        if (!userRepository.existsById(playerId)) {
            throw new ResourceNotFoundException("User not found: " + playerId);
        }

        List<Match> matches = matchRepository.findConfirmedMatchesByPlayerId(playerId).stream()
                .sorted(Comparator.comparing(Match::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        long totalMatches = matchRepository.countConfirmedMatchesByPlayerId(playerId);
        long totalMatchesAsDefender = matchRepository.countConfirmedMatchesAsDefender(playerId);
        long totalGoalsAsAttacker = matchRepository.sumGoalsAsAttacker(playerId);
        long totalWins = matches.stream().filter(m -> InsightMatchUtils.isPlayerWinner(m, playerId)).count();

        PlayerStatsContext stats = new PlayerStatsContext(
                playerId,
                totalMatches,
                totalWins,
                totalGoalsAsAttacker,
                totalMatchesAsDefender
        );

        List<AchievementDto> achievements = achievementService.getPlayerAchievements(playerId).achievements();

        return generateInsights(playerId, matches, stats, achievements);
    }

    @Override
    public PlayerInsightsResponse generateInsights(
            UUID playerId,
            List<Match> matches,
            PlayerStatsContext stats,
            List<AchievementDto> achievements
    ) {
        if (matches == null || matches.size() < 3) {
            PlayerInsightDto starterInsight = new PlayerInsightDto(
                    UUID.randomUUID(),
                    InsightType.INSUFFICIENT_DATA,
                    InsightCategory.GENERAL,
                    InsightImportance.LOW,
                    "insights.empty",
                    "insights.empty",
                    Map.of(),
                    "info",
                    null
            );
            return new PlayerInsightsResponse(playerId, 1, List.of(starterInsight));
        }

        List<PlayerInsightDto> insights = generators.stream()
                .sorted(Comparator.comparingInt(InsightGenerator::getOrder))
                .map(generator -> generator.generate(playerId, matches, stats, achievements))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(PlayerInsightDto::importance))
                .limit(5)
                .toList();

        return new PlayerInsightsResponse(playerId, insights.size(), insights);
    }
}
