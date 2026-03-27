package com.tictactore.service;

import com.tictactore.dto.statistics.*;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<LeaderboardEntryResponse> getLeaderboard(
            LeaderboardType type, 
            TimePeriod period, 
            Integer minMatches, 
            Pageable pageable) {
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(period, "period is required");
        Objects.requireNonNull(pageable, "pageable is required");
        
        log.debug("Calculating leaderboard: type={}, period={}, minMatches={}", type, period, minMatches);
        
        var since = calculateSinceDate(period);
        var typeStr = type.name();
        
        var projections = matchRepository.findLeaderboard(typeStr, since, minMatches, pageable);
        
        var startRank = (int) pageable.getOffset() + 1;
        var rankCounter = new AtomicInteger(startRank);

        return projections.map(p -> LeaderboardEntryResponse.builder()
                .rank(rankCounter.getAndIncrement())
                .playerId(UUID.fromString(p.getPlayerId()))
                .playerName(p.getPlayerName())
                .totalMatches(p.getTotalMatches())
                .wins(p.getWins())
                .losses(p.getLosses())
                .winRate(p.getWinRate())
                .build());
    }

    private LocalDateTime calculateSinceDate(TimePeriod period) {
        return switch (period) {
            case WEEKLY -> LocalDateTime.now().minusWeeks(1);
            case MONTHLY -> LocalDateTime.now().minusMonths(1);
            case YEARLY -> LocalDateTime.now().minusYears(1);
            case ALL_TIME -> LocalDate.of(2000, 1, 1).atStartOfDay();
        };
    }

    @Transactional(readOnly = true)
    public PlayerStatsResponse getPersonalStats(TimePeriod period) {
        Objects.requireNonNull(period, "period is required");
        var user = getCurrentUser();
        log.debug("Calculating personal stats for user={} with period={}", user.getEmail(), period);
        
        var since = calculateSinceDate(period);
        var p = matchRepository.findPlayerStats(user.getId(), since);
        
        return PlayerStatsResponse.builder()
                .playerId(user.getId())
                .playerName(user.getName())
                .overall(createPositionStats(p.getOverallMatches(), p.getOverallWins()))
                .attacker(createPositionStats(p.getAttackerMatches(), p.getAttackerWins()))
                .defender(createPositionStats(p.getDefenderMatches(), p.getDefenderWins()))
                .build();
    }

    @Transactional(readOnly = true)
    public Page<H2HStatsResponse> getH2HStats(
            TimePeriod period, 
            LeaderboardType myPosition, 
            LeaderboardType opponentPosition, 
            Pageable pageable) {
        Objects.requireNonNull(period, "period is required");
        Objects.requireNonNull(myPosition, "myPosition is required");
        Objects.requireNonNull(opponentPosition, "opponentPosition is required");
        Objects.requireNonNull(pageable, "pageable is required");
        
        var user = getCurrentUser();
        log.debug("Calculating H2H stats for user={} with period={}, myPosition={}, opponentPosition={}", 
                user.getEmail(), period, myPosition, opponentPosition);
        
        var since = calculateSinceDate(period);
        var projections = matchRepository.findH2HStats(
                user.getId(), 
                since, 
                myPosition.name(), 
                opponentPosition.name(), 
                pageable);
        
        return projections.map(p -> H2HStatsResponse.builder()
                .opponentId(UUID.fromString(p.getOpponentId()))
                .opponentName(p.getOpponentName())
                .matches(p.getTotalMatches())
                .wins(p.getWins())
                .losses(p.getLosses())
                .winRate(p.getWinRate())
                .build());
    }

    private User getCurrentUser() {
        var email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElseThrow(() -> new IllegalStateException("Authentication context is missing"));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public PlayerStatsResponse calculateStatsFromMatches(User player, List<Match> matches) {
        Objects.requireNonNull(player, "player is required");
        Objects.requireNonNull(matches, "matches list is required");

        var playerId = player.getId();
        var playerName = player.getName();
        
        var overallMatches = 0L;
        var overallWins = 0L;
        var attackerMatches = 0L;
        var attackerWins = 0L;
        var defenderMatches = 0L;
        var defenderWins = 0L;

        for (var m : matches) {
            var isWin = m.isWinner(player);
            overallMatches++;
            if (isWin) overallWins++;

            if (m.isAttacker(player)) {
                attackerMatches++;
                if (isWin) attackerWins++;
            }
            if (m.isDefender(player)) {
                defenderMatches++;
                if (isWin) defenderWins++;
            }
        }

        return PlayerStatsResponse.builder()
                .playerId(playerId)
                .playerName(playerName)
                .overall(createPositionStats(overallMatches, overallWins))
                .attacker(createPositionStats(attackerMatches, attackerWins))
                .defender(createPositionStats(defenderMatches, defenderWins))
                .build();
    }

    private PositionStatsResponse createPositionStats(long total, long wins) {
        var losses = total - wins;
        var winRate = calculateWinRate(wins, total);
        return new PositionStatsResponse(total, wins, losses, winRate);
    }

    private Double calculateWinRate(long wins, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((double) wins / total * 10000.0) / 100.0;
    }
}
