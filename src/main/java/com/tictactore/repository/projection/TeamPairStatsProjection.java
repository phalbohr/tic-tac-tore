package com.tictactore.repository.projection;

public interface TeamPairStatsProjection {
    String getAttackerId();
    String getDefenderId();
    long getMatches();
    long getWins();
    long getLosses();
    Double getWinRate();
}
