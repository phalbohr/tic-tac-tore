package com.tictactore.service.tournament.impl;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.exception.TournamentConflictException;
import com.tictactore.exception.TournamentRuleMismatchException;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.service.tournament.TournamentMatchValidator;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class TournamentMatchValidatorImpl implements TournamentMatchValidator {

    @Override
    public void validateTournamentMatchCreation(TournamentMatch tournamentMatch, CreateMatchRequest request) {
        if (tournamentMatch == null) {
            throw new TournamentConflictException("Tournament match cannot be null");
        }

        Tournament tournament = tournamentMatch.getTournament();
        if (tournament == null || tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new TournamentConflictException("Tournament is not in progress");
        }

        if (tournamentMatch.getMatch() != null || tournamentMatch.getStatus() == TournamentMatchStatus.COMPLETED) {
            throw new TournamentConflictException("Tournament match has already been completed");
        }

        if (tournamentMatch.getStatus() != TournamentMatchStatus.IN_PROGRESS && tournamentMatch.getStatus() != TournamentMatchStatus.READY) {
            throw new TournamentConflictException("Tournament match is not in a playable state");
        }

        validateMatchFormat(tournament.getMode(), request);

        if (request.ruleConfigId() == null) {
            throw new TournamentRuleMismatchException("Rule configuration ID is required for tournament matches");
        }

        var tournamentRuleConfig = tournament.getRuleConfiguration();
        if (tournamentRuleConfig == null || !request.ruleConfigId().equals(tournamentRuleConfig.getId())) {
            throw new TournamentRuleMismatchException(
                    "Match rule configuration (" + request.ruleConfigId() + ") does not match tournament rule configuration"
            );
        }

        validateParticipants(tournamentMatch, request);
    }

    private void validateMatchFormat(TournamentMode mode, CreateMatchRequest request) {
        if (mode == null) {
            return;
        }
        boolean is2v2Request = request.teamADefenderId() != null || request.teamBDefenderId() != null;
        if (mode == TournamentMode.ONE_VS_ONE_PERSONAL && is2v2Request) {
            throw new TournamentConflictException("Match format does not match tournament mode: expected 1v1");
        }
        if ((mode == TournamentMode.TWO_VS_TWO_FIXED_TEAMS || mode == TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS) && !is2v2Request) {
            throw new TournamentConflictException("Match format does not match tournament mode: expected 2v2");
        }
    }

    private void validateParticipants(TournamentMatch tournamentMatch, CreateMatchRequest request) {
        Set<UUID> expectedTeam1 = new HashSet<>();
        addRegistrationParticipants(expectedTeam1, tournamentMatch.getParticipant1());
        addRegistrationParticipants(expectedTeam1, tournamentMatch.getParticipant1Partner());

        Set<UUID> expectedTeam2 = new HashSet<>();
        addRegistrationParticipants(expectedTeam2, tournamentMatch.getParticipant2());
        addRegistrationParticipants(expectedTeam2, tournamentMatch.getParticipant2Partner());

        Set<UUID> reqTeamA = new HashSet<>();
        if (request.teamAAttackerId() != null) reqTeamA.add(request.teamAAttackerId());
        if (request.teamADefenderId() != null) reqTeamA.add(request.teamADefenderId());

        Set<UUID> reqTeamB = new HashSet<>();
        if (request.teamBAttackerId() != null) reqTeamB.add(request.teamBAttackerId());
        if (request.teamBDefenderId() != null) reqTeamB.add(request.teamBDefenderId());

        boolean validSides = (reqTeamA.equals(expectedTeam1) && reqTeamB.equals(expectedTeam2))
                || (reqTeamA.equals(expectedTeam2) && reqTeamB.equals(expectedTeam1));

        if (!validSides) {
            throw new TournamentConflictException("Participants do not match assigned tournament match roster");
        }
    }

    private void addRegistrationParticipants(Set<UUID> participants, TournamentRegistration registration) {
        if (registration == null) {
            return;
        }
        if (registration.getPlayer() != null && registration.getPlayer().getId() != null) {
            participants.add(registration.getPlayer().getId());
        }
        if (registration.getPartner() != null && registration.getPartner().getId() != null) {
            participants.add(registration.getPartner().getId());
        }
    }
}
