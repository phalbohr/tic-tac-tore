package com.tictactore.repository;

/**
 * Interface-based projection for aggregate Head-to-Head statistics.
 */
public interface H2HProjection {
    String getOpponentId();
    String getOpponentName();
    Long getTotalMatches();
    Long getWins();
    Long getLosses();
    Double getWinRate();
}
