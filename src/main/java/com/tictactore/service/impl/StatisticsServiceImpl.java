package com.tictactore.service.impl;

import com.tictactore.dto.PagedResponse;
import com.tictactore.dto.TeamPairStatsResponse;
import com.tictactore.dto.TimePeriod;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.repository.projection.TeamPairStatsProjection;
import com.tictactore.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    @Override
    public PagedResponse<TeamPairStatsResponse> getTeamPairStats(
            UUID playerId,
            TimePeriod period,
            UUID ruleConfigId,
            int page,
            int size,
            int minMatches
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        int safeMinMatches = Math.max(1, minMatches);

        Instant startDate = period != null ? period.getStartDate() : null;
        PageRequest pageRequest = PageRequest.of(safePage, safeSize);

        Page<TeamPairStatsProjection> projectionsPage = matchRepository.aggregateTeamPairStats(
                playerId,
                startDate,
                ruleConfigId,
                safeMinMatches,
                pageRequest
        );

        List<TeamPairStatsProjection> projections = projectionsPage.getContent();
        Map<UUID, User> userMap = resolveUsers(projections);

        List<TeamPairStatsResponse> responses = projections.stream()
                .map(p -> toResponse(p, userMap))
                .toList();

        return new PagedResponse<>(
                responses,
                projectionsPage.getNumber(),
                projectionsPage.getSize(),
                projectionsPage.getTotalElements(),
                projectionsPage.getTotalPages()
        );
    }

    private Map<UUID, User> resolveUsers(List<TeamPairStatsProjection> projections) {
        Set<UUID> userIds = new HashSet<>();
        for (TeamPairStatsProjection p : projections) {
            UUID attackerId = parseUuid(p.getAttackerId());
            UUID defenderId = parseUuid(p.getDefenderId());
            if (attackerId != null) {
                userIds.add(attackerId);
            }
            if (defenderId != null) {
                userIds.add(defenderId);
            }
        }

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return userRepository.findAllById(userIds).stream()
                    .filter(u -> u.getId() != null)
                    .collect(Collectors.toMap(User::getId, u -> u));
        } catch (Exception e) {
            log.warn("Failed to batch resolve user profiles for statistics", e);
            return Collections.emptyMap();
        }
    }

    private TeamPairStatsResponse toResponse(TeamPairStatsProjection projection, Map<UUID, User> userMap) {
        UUID attackerId = parseUuid(projection.getAttackerId());
        UUID defenderId = parseUuid(projection.getDefenderId());

        User attacker = attackerId != null ? userMap.get(attackerId) : null;
        User defender = defenderId != null ? userMap.get(defenderId) : null;

        String attackerName = resolveDisplayName(attacker);
        String defenderName = resolveDisplayName(defender);
        String attackerAvatar = attacker != null ? attacker.getAvatar() : null;
        String defenderAvatar = defender != null ? defender.getAvatar() : null;

        double winRate = projection.getWinRate() != null ? projection.getWinRate() : 0.0;

        return new TeamPairStatsResponse(
                attackerId,
                attackerName,
                attackerAvatar,
                defenderId,
                defenderName,
                defenderAvatar,
                projection.getMatches(),
                projection.getWins(),
                projection.getLosses(),
                winRate
        );
    }

    private UUID parseUuid(String uuidStr) {
        if (uuidStr == null || uuidStr.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(uuidStr.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID string in projection: {}", uuidStr);
            return null;
        }
    }

    private String resolveDisplayName(User user) {
        if (user == null) {
            return "A player";
        }
        if (user.getNickname() != null && user.getNickname().startsWith("ex-player-")) {
            return "A retired player";
        }
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return "A player";
    }
}
