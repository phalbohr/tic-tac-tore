package com.tictactore.service.tournament.impl;

import com.tictactore.dto.TournamentResponse;
import com.tictactore.event.TournamentCancelledEvent;
import com.tictactore.event.TournamentStartedEvent;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.exception.TournamentConflictException;
import com.tictactore.model.RegistrationStatus;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRegistrationRepository;
import com.tictactore.repository.TournamentRepository;
import com.tictactore.service.tournament.BracketGenerator;
import com.tictactore.service.tournament.TournamentLifecycleService;
import com.tictactore.service.tournament.TournamentSeedingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TournamentLifecycleServiceImpl implements TournamentLifecycleService {

    private final TournamentRepository tournamentRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentMatchRepository tournamentMatchRepository;
    private final TournamentSeedingStrategy seedingStrategy;
    @Qualifier("cupBracketGenerator")
    private final BracketGenerator cupBracketGenerator;
    @Qualifier("championshipBracketGenerator")
    private final BracketGenerator championshipBracketGenerator;
    @Qualifier("randomPairingBracketGenerator")
    private final BracketGenerator randomPairingBracketGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public TournamentResponse startTournament(UUID tournamentId) {
        Tournament tournament = tournamentRepository.findByIdWithLock(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId.toString()));

        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new TournamentConflictException("Tournament is not in REGISTRATION_OPEN status: " + tournament.getStatus());
        }

        List<TournamentRegistration> confirmedRegistrations =
                registrationRepository.findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED);

        if (confirmedRegistrations.size() < tournament.getMinParticipants()) {
            return cancelTournamentDueToLowCapacity(tournament, confirmedRegistrations);
        }

        return proceedStartTournament(tournament, confirmedRegistrations);
    }

    private TournamentResponse cancelTournamentDueToLowCapacity(
            Tournament tournament,
            List<TournamentRegistration> confirmedRegistrations
    ) {
        tournament.setStatus(TournamentStatus.CANCELLED);
        Tournament saved = tournamentRepository.save(tournament);

        List<UUID> participantUserIds = extractParticipantUserIds(confirmedRegistrations);
        String reason = String.format("Insufficient confirmed participants (%d/%d)",
                confirmedRegistrations.size(), tournament.getMinParticipants());

        eventPublisher.publishEvent(new TournamentCancelledEvent(
                saved.getId(),
                saved.getName(),
                reason,
                participantUserIds
        ));

        log.info("Tournament {} was CANCELLED due to insufficient participants ({}/{})",
                saved.getId(), confirmedRegistrations.size(), tournament.getMinParticipants());

        return mapToTournamentResponse(saved);
    }

    private TournamentResponse proceedStartTournament(
            Tournament tournament,
            List<TournamentRegistration> confirmedRegistrations
    ) {
        tournament.setStatus(TournamentStatus.IN_PROGRESS);

        var seededParticipants = seedingStrategy.seed(tournament, confirmedRegistrations);

        BracketGenerator generator = (tournament.getMode() == TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS)
                ? randomPairingBracketGenerator
                : (tournament.getFormat() == TournamentFormat.CHAMPIONSHIP ? championshipBracketGenerator : cupBracketGenerator);

        List<TournamentMatch> matches = generator.generateBracket(tournament, seededParticipants);
        tournamentMatchRepository.saveAll(matches);

        Tournament saved = tournamentRepository.save(tournament);

        List<UUID> participantUserIds = extractParticipantUserIds(confirmedRegistrations);
        eventPublisher.publishEvent(new TournamentStartedEvent(
                saved.getId(),
                saved.getName(),
                saved.getFormat(),
                saved.getMode(),
                participantUserIds,
                matches.size()
        ));

        log.info("Tournament {} started successfully with {} participants and {} matches",
                saved.getId(), seededParticipants.size(), matches.size());

        return mapToTournamentResponse(saved);
    }

    private List<UUID> extractParticipantUserIds(List<TournamentRegistration> registrations) {
        List<UUID> userIds = new ArrayList<>();
        for (TournamentRegistration reg : registrations) {
            if (reg.getPlayer() != null && reg.getPlayer().getId() != null) {
                userIds.add(reg.getPlayer().getId());
            }
            if (reg.getPartner() != null && reg.getPartner().getId() != null) {
                userIds.add(reg.getPartner().getId());
            }
        }
        return userIds;
    }

    private TournamentResponse mapToTournamentResponse(Tournament tournament) {
        return TournamentResponse.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .format(tournament.getFormat())
                .mode(tournament.getMode())
                .ruleConfigurationId(tournament.getRuleConfiguration() != null ? tournament.getRuleConfiguration().getId() : null)
                .minParticipants(tournament.getMinParticipants())
                .maxParticipants(tournament.getMaxParticipants())
                .registrationDeadline(tournament.getRegistrationDeadline())
                .roundCount(tournament.getRoundCount())
                .hasPlayoff(tournament.isHasPlayoff())
                .status(tournament.getStatus())
                .creatorId(tournament.getCreator() != null ? tournament.getCreator().getId() : null)
                .createdAt(tournament.getCreatedAt())
                .updatedAt(tournament.getUpdatedAt())
                .build();
    }
}
