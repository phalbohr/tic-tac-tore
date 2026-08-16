package com.tictactore.service.impl;

import com.tictactore.dto.LeaderboardEntry;
import com.tictactore.dto.PageResponse;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.Position;
import com.tictactore.repository.LeaderboardRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.LeaderboardService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;

    public LeaderboardServiceImpl(LeaderboardRepository leaderboardRepository, UserRepository userRepository) {
        this.leaderboardRepository = leaderboardRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PageResponse<LeaderboardEntry> getLeaderboard(String type, String period, int minMatches, String matchType, String matchFormat, int page, int size) {
        Instant startDate = resolveStartDate(period);

        List<Match> matches = leaderboardRepository.findConfirmedMatchesWithFilters(
            matchFormat, matchType, startDate, Instant.now()
        );

        Map<UUID, PlayerStats> statsMap = new HashMap<>();

        for (Match match : matches) {
            if (startDate != null && match.getCreatedAt().isBefore(startDate)) {
                continue;
            }
            if (matchFormat != null && !matchFormat.isBlank() && !matchFormat.equals(match.getMatchFormat())) {
                continue;
            }
            if (matchType != null && !matchType.isBlank()) {
                boolean is1v1 = match.getTeamADefenderId() == null && match.getTeamBDefenderId() == null;
                if ("1v1".equals(matchType) && !is1v1) continue;
                if ("2v2".equals(matchType) && is1v1) continue;
            }

            int teamAGames = 0;
            int teamBGames = 0;
            for (Game game : match.getGames()) {
                if (game.getTeamAScore() > game.getTeamBScore()) {
                    teamAGames++;
                } else if (game.getTeamBScore() > game.getTeamAScore()) {
                    teamBGames++;
                }
            }

            if (teamAGames > teamBGames) {
                recordResult(statsMap, match, true, type);
            } else if (teamBGames > teamAGames) {
                recordResult(statsMap, match, false, type);
            } else {
                recordDraw(statsMap, match, type);
            }
        }

        List<LeaderboardEntry> entries = statsMap.values().stream()
            .filter(s -> s.totalMatches >= minMatches)
            .map(s -> {
                double winRate = s.totalMatches > 0 ? (double) s.wins / s.totalMatches : 0.0;
                return new LeaderboardEntry(s.playerId, s.playerName, s.totalMatches, s.wins, s.losses, winRate);
            })
            .sorted(Comparator.comparingDouble((LeaderboardEntry e) -> e.winRate()).reversed()
                .thenComparingInt(e -> e.wins())
                .thenComparing(e -> e.playerName()))
            .toList();

        int totalElements = entries.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        int start = page * size;
        int end = Math.min(start + size, totalElements);
        List<LeaderboardEntry> pageContent = start >= totalElements ? List.of() : entries.subList(start, end);

        return new PageResponse<>(pageContent, totalPages, totalElements, size, page);
    }

    private Instant resolveStartDate(String period) {
        if (period == null || period.isBlank() || "ALL_TIME".equals(period)) {
            return null;
        }
        Instant now = Instant.now();
        return switch (period) {
            case "WEEKLY" -> now.minus(7, ChronoUnit.DAYS);
            case "MONTHLY" -> now.minus(30, ChronoUnit.DAYS);
            case "YEARLY" -> now.minus(365, ChronoUnit.DAYS);
            default -> null;
        };
    }

    private void recordDraw(Map<UUID, PlayerStats> statsMap, Match match, String type) {
        Set<UUID> teamAPlayers = getPlayersForTeam(match, true);
        Set<UUID> teamBPlayers = getPlayersForTeam(match, false);
        Set<UUID> allPlayers = new HashSet<>();
        allPlayers.addAll(teamAPlayers);
        allPlayers.addAll(teamBPlayers);

        for (UUID playerId : allPlayers) {
            if (playerId == null) continue;
            if (!isPlayerInPosition(match, playerId, type)) continue;
            PlayerStats stats = statsMap.computeIfAbsent(playerId, id -> {
                PlayerStats s = new PlayerStats();
                s.playerId = id;
                s.playerName = userRepository.findById(id).map(u -> u.getNickname()).orElse("Unknown");
                return s;
            });
            stats.totalMatches++;
        }
    }

    private void recordResult(Map<UUID, PlayerStats> statsMap, Match match, boolean teamAWon, String type) {
        Set<UUID> teamAPlayers = getPlayersForTeam(match, true);
        Set<UUID> teamBPlayers = getPlayersForTeam(match, false);

        Set<UUID> winners = teamAWon ? teamAPlayers : teamBPlayers;
        Set<UUID> losers = teamAWon ? teamBPlayers : teamAPlayers;

        for (UUID playerId : winners) {
            if (playerId == null) continue;
            if (!isPlayerInPosition(match, playerId, type)) continue;
            PlayerStats stats = statsMap.computeIfAbsent(playerId, id -> {
                PlayerStats s = new PlayerStats();
                s.playerId = id;
                s.playerName = userRepository.findById(id).map(u -> u.getNickname()).orElse("Unknown");
                return s;
            });
            stats.totalMatches++;
            stats.wins++;
        }
        for (UUID playerId : losers) {
            if (playerId == null) continue;
            if (!isPlayerInPosition(match, playerId, type)) continue;
            PlayerStats stats = statsMap.computeIfAbsent(playerId, id -> {
                PlayerStats s = new PlayerStats();
                s.playerId = id;
                s.playerName = userRepository.findById(id).map(u -> u.getNickname()).orElse("Unknown");
                return s;
            });
            stats.totalMatches++;
            stats.losses++;
        }
    }

    private Set<UUID> getPlayersForTeam(Match match, boolean teamA) {
        Set<UUID> players = new HashSet<>();
        if (teamA) {
            players.add(match.getTeamAAttackerId());
            if (match.getTeamADefenderId() != null) players.add(match.getTeamADefenderId());
        } else {
            players.add(match.getTeamBAttackerId());
            if (match.getTeamBDefenderId() != null) players.add(match.getTeamBDefenderId());
        }
        return players;
    }

    private boolean isPlayerInPosition(Match match, UUID playerId, String type) {
        if (type == null || type.isBlank() || "OVERALL".equals(type)) {
            return true;
        }
        if ("ATTACKER".equals(type)) {
            return playerId.equals(match.getTeamAAttackerId()) || playerId.equals(match.getTeamBAttackerId());
        } else {
            return playerId.equals(match.getTeamADefenderId()) || playerId.equals(match.getTeamBDefenderId());
        }
    }

    private static class PlayerStats {
        UUID playerId;
        String playerName;
        int totalMatches = 0;
        int wins = 0;
        int losses = 0;
    }
}
