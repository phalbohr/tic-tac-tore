package com.tictactore.service.tournament.impl;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.exception.TournamentConflictException;
import com.tictactore.exception.TournamentRuleMismatchException;
import com.tictactore.model.TournamentMatch;
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

        if (tournamentMatch.getTournament() == null || tournamentMatch.getTournament().getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new TournamentConflictException("Tournament is not in progress");
        }

        if (request.ruleConfigId() == null) {
            throw new TournamentRuleMismatchException("Rule configuration ID is required for tournament matches");
        }

        var tournamentRuleConfig = tournamentMatch.getTournament().getRuleConfiguration();
        if (tournamentRuleConfig == null || !request.ruleConfigId().equals(tournamentRuleConfig.getId())) {
            throw new TournamentRuleMismatchException(
                    "Match rule configuration (" + request.ruleConfigId() + ") does not match tournament rule configuration"
            );
        }

        validateParticipants(tournamentMatch, request);
    }

    private void validateParticipants(TournamentMatch tournamentMatch, CreateMatchRequest request) {
        Set<UUID> expectedParticipants = new HashSet<>();
        addRegistrationParticipants(expectedParticipants, tournamentMatch.getParticipant1());
        addRegistrationParticipants(expectedParticipants, tournamentMatch.getParticipant1Partner());
        addRegistrationParticipants(expectedParticipants, tournamentMatch.getParticipant2());
        addRegistrationParticipants(expectedParticipants, tournamentMatch.getParticipant2Partner());

        Set<UUID> requestParticipants = new HashSet<>();
        if (request.teamAAttackerId() != null) requestParticipants.add(request.teamAAttackerId());
        if (request.teamADefenderId() != null) requestParticipants.add(request.teamADefenderId());
        if (request.teamBAttackerId() != null) requestParticipants.add(request.teamBAttackerId());
        if (request.teamBDefenderId() != null) requestParticipants.add(request.teamBDefenderId());

        if (!expectedParticipants.equals(requestParticipants)) {
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
