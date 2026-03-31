package com.tictactore.repository;

import java.util.UUID;

/**
 * Interface-based projection for aggregate leaderboard statistics.
 */
public interface LeaderboardProjection {
    String getPlayerId();
    String getPlayerName();
    Long getTotalMatches();
    Long getWins();
    Long getLosses();
    Double getWinRate();
}
