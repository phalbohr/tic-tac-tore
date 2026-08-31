package com.tictactore.service.insight;

import com.tictactore.model.Match;

import java.util.UUID;

public final class InsightMatchUtils {

    private InsightMatchUtils() {
    }

    public static boolean isPlayerWinner(Match match, UUID playerId) {
        if (match == null || playerId == null || match.getGames() == null || match.getGames().isEmpty()) {
            return false;
        }
        boolean inTeamA = playerId.equals(match.getTeamAAttackerId()) || playerId.equals(match.getTeamADefenderId());
        boolean inTeamB = playerId.equals(match.getTeamBAttackerId()) || playerId.equals(match.getTeamBDefenderId());
        if (!inTeamA && !inTeamB) {
            return false;
        }
        long winsA = match.getGames().stream().filter(g -> g.getTeamAScore() > g.getTeamBScore()).count();
        long winsB = match.getGames().stream().filter(g -> g.getTeamBScore() > g.getTeamAScore()).count();
        return (inTeamA && winsA > winsB) || (inTeamB && winsB > winsA);
    }
}
