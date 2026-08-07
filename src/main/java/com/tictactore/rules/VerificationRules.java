package com.tictactore.rules;

import com.tictactore.model.Match;

import java.util.List;
import java.util.UUID;

public final class VerificationRules {

    private VerificationRules() {
    }

    public static int getRequiredConfirmations(Match match) {
        if (match == null) {
            return 1;
        }
        boolean isSingles = match.getTeamADefenderId() == null;
        boolean isParticipant = isParticipantEntered(match);

        if (isSingles) {
            return isParticipant ? 1 : 2;
        }
        return 2;
    }

    public static boolean supportsPartialConfirmation(Match match) {
        if (match == null) {
            return false;
        }
        boolean isDoubles = match.getTeamADefenderId() != null;
        return isDoubles
            && isParticipantEntered(match)
            && Match.MATCH_FORMAT_STANDARD.equals(match.getMatchFormat());
    }

    public static boolean isFullyConfirmed(Match match) {
        if (match == null) {
            return false;
        }
        if (Match.STATUS_CONFIRMED.equals(match.getStatus())) {
            return true;
        }
        if (Match.STATUS_REJECTED.equals(match.getStatus())) {
            return false;
        }

        List<UUID> confirmed = match.getConfirmedByOpponentIdsList();
        if (confirmed.isEmpty()) {
            return false;
        }

        boolean isSingles = match.getTeamADefenderId() == null;
        boolean isParticipant = isParticipantEntered(match);

        if (isSingles) {
            return isParticipant ? confirmed.size() >= 1 : confirmed.size() >= 2;
        }

        if (!isParticipant) {
            boolean hasTeamA = confirmed.stream()
                .anyMatch(id -> id.equals(match.getTeamAAttackerId()) || id.equals(match.getTeamADefenderId()));
            boolean hasTeamB = confirmed.stream()
                .anyMatch(id -> id.equals(match.getTeamBAttackerId()) || id.equals(match.getTeamBDefenderId()));
            return hasTeamA && hasTeamB;
        }

        return confirmed.size() >= 2;
    }

    public static boolean isPartiallyConfirmed(Match match) {
        if (match == null) {
            return false;
        }
        return Match.STATUS_PARTIALLY_CONFIRMED.equals(match.getStatus());
    }

    private static boolean isParticipantEntered(Match match) {
        UUID creatorId = match.getCreatorId();
        if (creatorId == null) {
            return true;
        }
        return creatorId.equals(match.getTeamAAttackerId())
            || creatorId.equals(match.getTeamADefenderId())
            || creatorId.equals(match.getTeamBAttackerId())
            || creatorId.equals(match.getTeamBDefenderId());
    }
}
