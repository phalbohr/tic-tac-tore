package com.tictactore.rules;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.model.Match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
            && (Match.MATCH_FORMAT_STANDARD.equals(match.getMatchFormat())
                || Match.MATCH_FORMAT_RANDOM.equals(match.getMatchFormat()));
    }

    public static boolean requiresCooldown(Match match) {
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

    public static boolean isUserPendingApprover(Match match, UUID userId) {
        if (userId == null || userId.equals(match.getCreatorId())) {
            return false;
        }
        if (match.hasConfirmed(userId)) {
            return false;
        }
        UUID creatorId = match.getCreatorId();
        boolean creatorOnTeamA = creatorId != null && (creatorId.equals(match.getTeamAAttackerId()) || creatorId.equals(match.getTeamADefenderId()));
        boolean creatorOnTeamB = creatorId != null && (creatorId.equals(match.getTeamBAttackerId()) || creatorId.equals(match.getTeamBDefenderId()));

        if (creatorOnTeamA) {
            return userId.equals(match.getTeamBAttackerId()) || userId.equals(match.getTeamBDefenderId());
        } else if (creatorOnTeamB) {
            return userId.equals(match.getTeamAAttackerId()) || userId.equals(match.getTeamADefenderId());
        } else {
            return userId.equals(match.getTeamAAttackerId()) || userId.equals(match.getTeamADefenderId())
                    || userId.equals(match.getTeamBAttackerId()) || userId.equals(match.getTeamBDefenderId());
        }
    }

    public static List<UUID> resolveOpponentIds(CreateMatchRequest request, Collection<UUID> allParticipants) {
        UUID creatorId = request.creatorId();
        boolean isOnTeamA = creatorId != null && (creatorId.equals(request.teamAAttackerId()) || creatorId.equals(request.teamADefenderId()));
        boolean isOnTeamB = creatorId != null && (creatorId.equals(request.teamBAttackerId()) || creatorId.equals(request.teamBDefenderId()));

        List<UUID> opponents = new ArrayList<>();
        if (isOnTeamA) {
            if (request.teamBAttackerId() != null) opponents.add(request.teamBAttackerId());
            if (request.teamBDefenderId() != null) opponents.add(request.teamBDefenderId());
        } else if (isOnTeamB) {
            if (request.teamAAttackerId() != null) opponents.add(request.teamAAttackerId());
            if (request.teamADefenderId() != null) opponents.add(request.teamADefenderId());
        } else {
            opponents.addAll(allParticipants);
            if (creatorId != null) {
                opponents.remove(creatorId);
            }
        }
        return opponents;
    }

    public static boolean isIdenticalMatch(Match candidate, Match current, Collection<UUID> currentParticipants) {
        return new HashSet<>(candidate.getParticipantIds()).equals(new HashSet<>(currentParticipants));
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
