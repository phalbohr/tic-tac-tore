package com.tictactore.repository;

/**
 * Interface-based projection for aggregate player statistics.
 */
public interface PlayerStatsProjection {
    Long getOverallMatches();
    Long getOverallWins();
    Long getAttackerMatches();
    Long getAttackerWins();
    Long getDefenderMatches();
    Long getDefenderWins();
}
