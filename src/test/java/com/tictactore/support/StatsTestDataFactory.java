package com.tictactore.support;

import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Test-data factory for leaderboard test fixtures.
 *
 * <p>Story 4.2: Global Leaderboard with Filtering.
 * Builds {@link Match}/{@link Game}/{@link User} fixtures with CONFIRMED status
 * and configurable team structure (1v1 / 2v2), game scores, match format, and
 * timestamps so that aggregation, filtering, threshold, and pagination
 * scenarios can be seeded into an H2 test database.
 */
public final class StatsTestDataFactory {

    private StatsTestDataFactory() {
    }

    public static User user(UUID id, String nickname) {
        return User.builder()
                .id(id)
                .email(emailFor(nickname))
                .nickname(nickname)
                .build();
    }

    private static String emailFor(String nickname) {
        return nickname.toLowerCase().replace(" ", ".") + "@example.com";
    }

    public static Match confirmedOneVOne(UUID teamAAttackerId, UUID teamBAttackerId,
                                         int teamAScore, int teamBScore, Instant createdAt) {
        return confirmedOneVOne(teamAAttackerId, teamBAttackerId, teamAScore, teamBScore,
                createdAt, Match.MATCH_FORMAT_STANDARD);
    }

    public static Match confirmedOneVOne(UUID teamAAttackerId, UUID teamBAttackerId,
                                         int teamAScore, int teamBScore, Instant createdAt,
                                         String matchFormat) {
        Match match = baseConfirmed(createdAt, matchFormat);
        match.setTeamAAttackerId(teamAAttackerId);
        match.setTeamADefenderId(null);
        match.setTeamBAttackerId(teamBAttackerId);
        match.setTeamBDefenderId(null);
        match.setCreatorId(teamAAttackerId);
        withGame(match, teamAScore, teamBScore);
        return match;
    }

    public static Match confirmedTwoVTwo(UUID aAttacker, UUID aDefender,
                                         UUID bAttacker, UUID bDefender,
                                         int teamAScore, int teamBScore, Instant createdAt) {
        return confirmedTwoVTwo(aAttacker, aDefender, bAttacker, bDefender,
                teamAScore, teamBScore, createdAt, Match.MATCH_FORMAT_STANDARD);
    }

    public static Match confirmedTwoVTwo(UUID aAttacker, UUID aDefender,
                                         UUID bAttacker, UUID bDefender,
                                         int teamAScore, int teamBScore, Instant createdAt,
                                         String matchFormat) {
        Match match = baseConfirmed(createdAt, matchFormat);
        match.setTeamAAttackerId(aAttacker);
        match.setTeamADefenderId(aDefender);
        match.setTeamBAttackerId(bAttacker);
        match.setTeamBDefenderId(bDefender);
        match.setCreatorId(aAttacker);
        withGame(match, teamAScore, teamBScore);
        return match;
    }

    public static Match pendingOneVOne(UUID teamAAttackerId, UUID teamBAttackerId,
                                       int teamAScore, int teamBScore, Instant createdAt) {
        Match match = baseMatch(createdAt, Match.MATCH_FORMAT_STANDARD);
        match.setStatus(Match.STATUS_PENDING_APPROVAL);
        match.setTeamAAttackerId(teamAAttackerId);
        match.setTeamADefenderId(null);
        match.setTeamBAttackerId(teamBAttackerId);
        match.setTeamBDefenderId(null);
        match.setCreatorId(teamAAttackerId);
        withGame(match, teamAScore, teamBScore);
        return match;
    }

    private static Match baseConfirmed(Instant createdAt, String matchFormat) {
        Match match = baseMatch(createdAt, matchFormat);
        match.setStatus(Match.STATUS_CONFIRMED);
        return match;
    }

    private static Match baseMatch(Instant createdAt, String matchFormat) {
        Match match = Match.builder()
                .matchFormat(matchFormat)
                .createdAt(createdAt)
                .build();
        return match;
    }

    private static void withGame(Match match, int teamAScore, int teamBScore) {
        match.addGame(new Game(null, null, 1, teamAScore, teamBScore,
                null, null, null, null, null));
    }
}
