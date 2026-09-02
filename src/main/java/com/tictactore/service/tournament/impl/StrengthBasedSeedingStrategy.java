package com.tictactore.service.tournament.impl;

import com.tictactore.dto.tournament.SeededParticipant;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.repository.MatchRepository;
import com.tictactore.service.tournament.TournamentSeedingStrategy;
import lombok.RequiredArgsConstructor;
import com.tictactore.repository.projection.PlayerMatchStatsProjection;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class StrengthBasedSeedingStrategy implements TournamentSeedingStrategy {

    private final MatchRepository matchRepository;

    @Override
    public List<SeededParticipant> seed(Tournament tournament, List<TournamentRegistration> registrations) {
        if (registrations == null || registrations.isEmpty()) {
            return List.of();
        }

        List<ParticipantStats> evaluated = new ArrayList<>();
        boolean is2v2 = tournament.getMode() == TournamentMode.TWO_VS_TWO_FIXED_TEAMS;
        Map<UUID, PlayerRecord> recordCache = new HashMap<>();

        for (TournamentRegistration registration : registrations) {
            ParticipantStats stats = evaluateRegistration(registration, is2v2, recordCache);
            evaluated.add(stats);
        }

        evaluated.sort(Comparator
                .comparing(ParticipantStats::hasMatchHistory).reversed()
                .thenComparing(Comparator.comparingDouble(ParticipantStats::strengthScore).reversed())
                .thenComparing(Comparator.comparingInt(ParticipantStats::totalWins).reversed())
                .thenComparing(ParticipantStats::createdAt)
                .thenComparing(ParticipantStats::registrationId));

        List<SeededParticipant> result = new ArrayList<>(evaluated.size());
        for (int i = 0; i < evaluated.size(); i++) {
            int seed = i + 1;
            ParticipantStats stats = evaluated.get(i);
            stats.registration().setSeed(seed);
            stats.registration().setStrengthScore(stats.strengthScore());
            result.add(new SeededParticipant(stats.registration(), seed, stats.strengthScore()));
        }

        return result;
    }

    private ParticipantStats evaluateRegistration(
            TournamentRegistration registration,
            boolean is2v2,
            Map<UUID, PlayerRecord> recordCache
    ) {
        PlayerRecord p1Record = recordCache.computeIfAbsent(registration.getPlayer().getId(), this::calculatePlayerRecord);

        if (is2v2 && registration.getPartner() != null) {
            PlayerRecord p2Record = recordCache.computeIfAbsent(registration.getPartner().getId(), this::calculatePlayerRecord);

            double combinedStrength;
            if (p1Record.hasMatchHistory() && p2Record.hasMatchHistory()) {
                combinedStrength = (p1Record.winRate() + p2Record.winRate()) / 2.0;
            } else if (p1Record.hasMatchHistory()) {
                combinedStrength = p1Record.winRate();
            } else if (p2Record.hasMatchHistory()) {
                combinedStrength = p2Record.winRate();
            } else {
                combinedStrength = 0.0;
            }

            int combinedWins = p1Record.wins() + p2Record.wins();
            boolean hasHistory = p1Record.hasMatchHistory() || p2Record.hasMatchHistory();

            return new ParticipantStats(
                    registration,
                    combinedStrength,
                    combinedWins,
                    hasHistory,
                    registration.getCreatedAt(),
                    registration.getId()
            );
        }

        return new ParticipantStats(
                registration,
                p1Record.winRate(),
                p1Record.wins(),
                p1Record.hasMatchHistory(),
                registration.getCreatedAt(),
                registration.getId()
        );
    }

    private PlayerRecord calculatePlayerRecord(UUID playerId) {
        try {
            PlayerMatchStatsProjection stats = matchRepository.getPlayerMatchStats(playerId);
            if (stats != null) {
                int totalMatches = (int) stats.getTotalMatches();
                int wins = (int) stats.getWins();
                double winRate = totalMatches > 0 ? (double) wins / totalMatches : 0.0;
                return new PlayerRecord(totalMatches, wins, winRate);
            }
        } catch (Exception ignored) {
            // Fallback to in-memory evaluation for testing mocks or dialects where native aggregation is omitted
        }

        List<Match> matches = matchRepository.findConfirmedMatchesByPlayerId(playerId);
        if (matches == null || matches.isEmpty()) {
            return new PlayerRecord(0, 0, 0.0);
        }

        int totalMatches = 0;
        int wins = 0;

        for (Match match : matches) {
            if (match.getGames() == null || match.getGames().isEmpty()) {
                continue;
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

            if (teamAGames == teamBGames) {
                totalMatches++;
                continue;
            }

            boolean isTeamA = playerId.equals(match.getTeamAAttackerId())
                    || (match.getTeamADefenderId() != null && playerId.equals(match.getTeamADefenderId()));
            boolean isTeamB = playerId.equals(match.getTeamBAttackerId())
                    || (match.getTeamBDefenderId() != null && playerId.equals(match.getTeamBDefenderId()));

            boolean won = (isTeamA && teamAGames > teamBGames) || (isTeamB && teamBGames > teamAGames);
            totalMatches++;
            if (won) {
                wins++;
            }
        }

        double winRate = totalMatches > 0 ? (double) wins / totalMatches : 0.0;
        return new PlayerRecord(totalMatches, wins, winRate);
    }

    private record PlayerRecord(int totalMatches, int wins, double winRate) {
        boolean hasMatchHistory() {
            return totalMatches > 0;
        }
    }

    private record ParticipantStats(
            TournamentRegistration registration,
            double strengthScore,
            int totalWins,
            boolean hasMatchHistory,
            java.time.Instant createdAt,
            UUID registrationId
    ) {}
}
