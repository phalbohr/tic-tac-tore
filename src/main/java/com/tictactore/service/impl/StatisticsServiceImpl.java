package com.tictactore.service.impl;

import com.tictactore.dto.*;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.Position;
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

    @Override
    public H2HStatsResponse getHeadToHeadStats(
            UUID playerId,
            UUID opponentId,
            TimePeriod period,
            UUID ruleConfigId,
            String matchType
    ) {
        if (playerId == null || opponentId == null) {
            throw new IllegalArgumentException("Player ID and Opponent ID must not be null");
        }

        User opponent = userRepository.findById(opponentId).orElse(null);
        String opponentNickname = resolveDisplayName(opponent);
        String opponentAvatar = opponent != null ? opponent.getAvatar() : null;
        PlayerSummaryDto opponentSummary = new PlayerSummaryDto(opponentId, opponentNickname, opponentAvatar);

        Instant startDate = period != null ? period.getStartDate() : null;
        List<Match> matches = matchRepository.findHeadToHeadMatches(playerId, opponentId, startDate, ruleConfigId, matchType);

        long withMatches = 0;
        long withWins = 0;
        long withLosses = 0;
        long withDraws = 0;

        long vsMatches = 0;
        long vsWins = 0;
        long vsLosses = 0;
        long vsDraws = 0;

        long withGamesWon = 0;
        long withGamesLost = 0;
        long withTotalGames = 0;

        long vsGamesWon = 0;
        long vsGamesLost = 0;
        long vsTotalGames = 0;

        long aVsDScored = 0;
        long aVsDConceded = 0;

        long aVsAScored = 0;
        long aVsAConceded = 0;

        long dVsAScored = 0;
        long dVsAConceded = 0;

        long dVsDScored = 0;
        long dVsDConceded = 0;

        for (Match match : matches) {
            boolean playerInTeamA = playerId.equals(match.getTeamAAttackerId()) || playerId.equals(match.getTeamADefenderId());
            boolean playerInTeamB = playerId.equals(match.getTeamBAttackerId()) || playerId.equals(match.getTeamBDefenderId());
            boolean oppInTeamA = opponentId.equals(match.getTeamAAttackerId()) || opponentId.equals(match.getTeamADefenderId());
            boolean oppInTeamB = opponentId.equals(match.getTeamBAttackerId()) || opponentId.equals(match.getTeamBDefenderId());

            if ((playerInTeamA && oppInTeamA) || (playerInTeamB && oppInTeamB)) {
                // "With" - Teammates in 2v2
                boolean isTeamA = playerInTeamA;
                int teamAGames = 0;
                int teamBGames = 0;

                if (match.getGames() != null) {
                    for (Game game : match.getGames()) {
                        withTotalGames++;
                        if (game.getTeamAScore() > game.getTeamBScore()) {
                            teamAGames++;
                            if (isTeamA) withGamesWon++; else withGamesLost++;
                        } else if (game.getTeamBScore() > game.getTeamAScore()) {
                            teamBGames++;
                            if (!isTeamA) withGamesWon++; else withGamesLost++;
                        }
                    }
                }

                withMatches++;
                int myTeamGames = isTeamA ? teamAGames : teamBGames;
                int oppTeamGames = isTeamA ? teamBGames : teamAGames;
                if (myTeamGames > oppTeamGames) {
                    withWins++;
                } else if (oppTeamGames > myTeamGames) {
                    withLosses++;
                } else {
                    withDraws++;
                }
            } else if ((playerInTeamA && oppInTeamB) || (playerInTeamB && oppInTeamA)) {
                // "Vs" - Opponents in 1v1 or 2v2
                boolean isTeamA = playerInTeamA;
                int teamAGames = 0;
                int teamBGames = 0;

                if (match.getGames() != null) {
                    for (Game game : match.getGames()) {
                        vsTotalGames++;
                        int myGameScore = isTeamA ? game.getTeamAScore() : game.getTeamBScore();
                        int oppGameScore = isTeamA ? game.getTeamBScore() : game.getTeamAScore();

                        if (myGameScore > oppGameScore) {
                            vsGamesWon++;
                            if (isTeamA) teamAGames++; else teamBGames++;
                        } else if (oppGameScore > myGameScore) {
                            vsGamesLost++;
                            if (isTeamA) teamBGames++; else teamAGames++;
                        }

                        Position playerPos = getPlayerPositionInGame(game, match, playerId);
                        Position oppPos = getPlayerPositionInGame(game, match, opponentId);

                        if (playerPos == Position.ATTACKER && oppPos == Position.DEFENDER) {
                            aVsDScored += myGameScore;
                            aVsDConceded += oppGameScore;
                        } else if (playerPos == Position.ATTACKER && oppPos == Position.ATTACKER) {
                            aVsAScored += myGameScore;
                            aVsAConceded += oppGameScore;
                        } else if (playerPos == Position.DEFENDER && oppPos == Position.ATTACKER) {
                            dVsAScored += myGameScore;
                            dVsAConceded += oppGameScore;
                        } else if (playerPos == Position.DEFENDER && oppPos == Position.DEFENDER) {
                            dVsDScored += myGameScore;
                            dVsDConceded += oppGameScore;
                        }
                    }
                }

                vsMatches++;
                int myTeamGames = isTeamA ? teamAGames : teamBGames;
                int oppTeamGames = isTeamA ? teamBGames : teamAGames;
                if (myTeamGames > oppTeamGames) {
                    vsWins++;
                } else if (oppTeamGames > myTeamGames) {
                    vsLosses++;
                } else {
                    vsDraws++;
                }
            }
        }

        double withMatchWinRate = withMatches > 0 ? roundOneDecimal((withWins * 100.0) / withMatches) : 0.0;
        double vsMatchWinRate = vsMatches > 0 ? roundOneDecimal((vsWins * 100.0) / vsMatches) : 0.0;
        H2HMatchTableDto matchTable = new H2HMatchTableDto(
                new H2HMatchStatsDto(withMatches, withWins, withLosses, withDraws, withMatchWinRate),
                new H2HMatchStatsDto(vsMatches, vsWins, vsLosses, vsDraws, vsMatchWinRate)
        );

        double withGameWinRate = withTotalGames > 0 ? roundOneDecimal((withGamesWon * 100.0) / withTotalGames) : 0.0;
        double vsGameWinRate = vsTotalGames > 0 ? roundOneDecimal((vsGamesWon * 100.0) / vsTotalGames) : 0.0;
        H2HGameTableDto gameTable = new H2HGameTableDto(
                new H2HGameStatsDto(withGamesWon, withGamesLost, withTotalGames, withGameWinRate),
                new H2HGameStatsDto(vsGamesWon, vsGamesLost, vsTotalGames, vsGameWinRate)
        );

        H2HGoalStatsDto goalStats = new H2HGoalStatsDto(
                new PositionalGoalMatrixDto(aVsDScored, aVsDConceded),
                new PositionalGoalMatrixDto(aVsAScored, aVsAConceded),
                new PositionalGoalMatrixDto(dVsAScored, dVsAConceded),
                new PositionalGoalMatrixDto(dVsDScored, dVsDConceded)
        );

        return new H2HStatsResponse(opponentSummary, matchTable, gameTable, goalStats);
    }

    private Position getPlayerPositionInGame(Game game, Match match, UUID userId) {
        if (userId == null) return Position.ATTACKER;
        if (userId.equals(game.getTeamAAttackerId()) || userId.equals(game.getTeamBAttackerId())) {
            return Position.ATTACKER;
        }
        if (userId.equals(game.getTeamADefenderId()) || userId.equals(game.getTeamBDefenderId())) {
            return Position.DEFENDER;
        }
        if (userId.equals(match.getTeamAAttackerId()) || userId.equals(match.getTeamBAttackerId())) {
            return Position.ATTACKER;
        }
        if (userId.equals(match.getTeamADefenderId()) || userId.equals(match.getTeamBDefenderId())) {
            return Position.DEFENDER;
        }
        return Position.ATTACKER;
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
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
