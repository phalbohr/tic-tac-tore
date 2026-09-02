package com.tictactore.service;

import com.tictactore.dto.MyRegistrationStatusResponse;
import com.tictactore.dto.RegisterTournamentRequest;
import com.tictactore.dto.TournamentRegistrationResponse;
import com.tictactore.event.TournamentInviteAcceptedEvent;
import com.tictactore.event.TournamentInviteCreatedEvent;
import com.tictactore.event.TournamentInviteDeclinedEvent;
import com.tictactore.event.TournamentRegistrationCancelledEvent;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.exception.TournamentConflictException;
import com.tictactore.model.RegistrationStatus;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import com.tictactore.repository.TournamentRegistrationRepository;
import com.tictactore.repository.TournamentRepository;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TournamentRegistrationServiceImpl implements TournamentRegistrationService {

    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public TournamentRegistrationResponse register(UUID tournamentId, UUID playerId, RegisterTournamentRequest request) {
        var tournament = tournamentRepository.findByIdWithLock(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId.toString()));

        validateTournamentOpenForRegistration(tournament);
        validateModeAndPartner(tournament, playerId, request.partnerId());
        validateNoActiveRegistration(tournamentId, playerId, request.partnerId());
        validateCapacity(tournament);

        var player = userRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", playerId.toString()));

        User partner = null;
        if (request.partnerId() != null) {
            partner = userRepository.findById(request.partnerId())
                    .orElseThrow(() -> new IllegalArgumentException("Partner not found: " + request.partnerId()));
        }

        var status = tournament.getMode() == TournamentMode.TWO_VS_TWO_FIXED_TEAMS
                ? RegistrationStatus.PENDING_CONFIRMATION
                : RegistrationStatus.CONFIRMED;

        var registration = TournamentRegistration.builder()
                .tournament(tournament)
                .player(player)
                .partner(partner)
                .status(status)
                .build();

        var saved = registrationRepository.save(registration);

        if (status == RegistrationStatus.PENDING_CONFIRMATION && partner != null) {
            eventPublisher.publishEvent(new TournamentInviteCreatedEvent(
                    saved.getId(),
                    tournament.getId(),
                    tournament.getName(),
                    player.getId(),
                    player.getNickname(),
                    partner.getId()
            ));
        }

        return mapToResponse(saved);
    }

    @Override
    public TournamentRegistrationResponse acceptInvitation(UUID tournamentId, UUID registrationId, UUID partnerId) {
        var registration = registrationRepository.findByIdWithDetails(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("TournamentRegistration", registrationId.toString()));

        validateRegistrationTournament(registration, tournamentId);

        var tournament = tournamentRepository.findByIdWithLock(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId.toString()));

        validateTournamentOpenForRegistration(tournament);
        validateCapacity(tournament);
        validatePartnerNoConfirmedRegistration(tournamentId, partnerId, registrationId);

        registration.accept(partnerId);
        var saved = registrationRepository.save(registration);

        eventPublisher.publishEvent(new TournamentInviteAcceptedEvent(
                saved.getId(),
                tournamentId,
                saved.getTournament().getName(),
                saved.getPartner().getId(),
                saved.getPartner().getNickname(),
                saved.getPlayer().getId()
        ));

        return mapToResponse(saved);
    }

    @Override
    public TournamentRegistrationResponse declineInvitation(UUID tournamentId, UUID registrationId, UUID partnerId) {
        var registration = registrationRepository.findByIdWithDetails(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("TournamentRegistration", registrationId.toString()));

        validateRegistrationTournament(registration, tournamentId);

        registration.decline(partnerId);
        var saved = registrationRepository.save(registration);

        eventPublisher.publishEvent(new TournamentInviteDeclinedEvent(
                saved.getId(),
                tournamentId,
                saved.getTournament().getName(),
                saved.getPartner().getId(),
                saved.getPartner().getNickname(),
                saved.getPlayer().getId()
        ));

        return mapToResponse(saved);
    }

    @Override
    public void cancelRegistration(UUID tournamentId, UUID registrationId, UUID userId) {
        var registration = registrationRepository.findByIdWithDetails(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("TournamentRegistration", registrationId.toString()));

        validateRegistrationTournament(registration, tournamentId);
        validateTournamentOpenForRegistration(registration.getTournament());

        User notifyRecipient = null;
        String cancellerName = null;
        if (registration.getPlayer() != null && registration.getPlayer().getId().equals(userId)) {
            cancellerName = registration.getPlayer().getNickname();
            notifyRecipient = registration.getPartner();
        } else if (registration.getPartner() != null && registration.getPartner().getId().equals(userId)) {
            cancellerName = registration.getPartner().getNickname();
            notifyRecipient = registration.getPlayer();
        }

        registration.cancel(userId);
        var saved = registrationRepository.save(registration);

        if (notifyRecipient != null) {
            eventPublisher.publishEvent(new TournamentRegistrationCancelledEvent(
                    saved.getId(),
                    tournamentId,
                    saved.getTournament().getName(),
                    userId,
                    cancellerName != null ? cancellerName : "Teammate",
                    notifyRecipient.getId()
            ));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentRegistrationResponse> listRegistrations(UUID tournamentId, RegistrationStatus status) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament", tournamentId.toString());
        }

        var list = (status != null)
                ? registrationRepository.findByTournamentIdAndStatus(tournamentId, status)
                : registrationRepository.findByTournamentId(tournamentId);

        return list.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MyRegistrationStatusResponse getMyRegistrationStatus(UUID tournamentId, UUID userId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament", tournamentId.toString());
        }

        var activeRegistration = registrationRepository.findActiveUserRegistration(
                tournamentId,
                userId,
                Set.of(RegistrationStatus.PENDING_CONFIRMATION, RegistrationStatus.CONFIRMED)
        );

        if (activeRegistration.isEmpty()) {
            return new MyRegistrationStatusResponse(false, null, false);
        }

        var reg = activeRegistration.get();
        boolean isPending = reg.getStatus() == RegistrationStatus.PENDING_CONFIRMATION;
        boolean isPartner = reg.getPartner() != null && reg.getPartner().getId().equals(userId);
        boolean isPendingInvite = isPending && isPartner;

        return new MyRegistrationStatusResponse(true, mapToResponse(reg), isPendingInvite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentRegistrationResponse> getPendingInvitations(UUID userId) {
        return registrationRepository.findPendingInvitationsForUser(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateTournamentOpenForRegistration(Tournament tournament) {
        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new TournamentConflictException("Tournament is not open for registration");
        }
        if (tournament.getRegistrationDeadline() != null && Instant.now().isAfter(tournament.getRegistrationDeadline())) {
            throw new IllegalArgumentException("Tournament registration deadline has passed");
        }
    }

    private void validateModeAndPartner(Tournament tournament, UUID playerId, UUID partnerId) {
        if (tournament.getMode() == TournamentMode.TWO_VS_TWO_FIXED_TEAMS) {
            if (partnerId == null) {
                throw new IllegalArgumentException("Partner ID is required for 2v2 fixed teams mode");
            }
            if (partnerId.equals(playerId)) {
                throw new IllegalArgumentException("Partner cannot be the same as the initiating player");
            }
        } else {
            if (partnerId != null) {
                throw new IllegalArgumentException("Partner cannot be specified for 1v1 or random pairings mode");
            }
        }
    }

    private void validateNoActiveRegistration(UUID tournamentId, UUID playerId, UUID partnerId) {
        var activeStatuses = Set.of(RegistrationStatus.PENDING_CONFIRMATION, RegistrationStatus.CONFIRMED);
        if (registrationRepository.findActiveUserRegistration(tournamentId, playerId, activeStatuses).isPresent()) {
            throw new TournamentConflictException("User already has an active registration for this tournament");
        }
        if (partnerId != null && registrationRepository.findActiveUserRegistration(tournamentId, partnerId, activeStatuses).isPresent()) {
            throw new TournamentConflictException("Partner already has an active registration for this tournament");
        }
    }

    private void validatePartnerNoConfirmedRegistration(UUID tournamentId, UUID partnerId, UUID registrationId) {
        var confirmed = registrationRepository.findActiveUserRegistration(tournamentId, partnerId, Set.of(RegistrationStatus.CONFIRMED));
        if (confirmed.isPresent() && !confirmed.get().getId().equals(registrationId)) {
            throw new TournamentConflictException("Partner already has an active confirmed registration for this tournament");
        }
    }

    private void validateCapacity(Tournament tournament) {
        long confirmedCount = registrationRepository.countByTournamentIdAndStatus(tournament.getId(), RegistrationStatus.CONFIRMED);
        if (confirmedCount >= tournament.getMaxParticipants()) {
            throw new TournamentConflictException("Tournament has reached maximum participant capacity");
        }
    }

    private void validateRegistrationTournament(TournamentRegistration registration, UUID tournamentId) {
        if (!registration.getTournament().getId().equals(tournamentId)) {
            throw new IllegalArgumentException("Registration does not belong to tournament");
        }
    }

    private TournamentRegistrationResponse mapToResponse(TournamentRegistration reg) {
        return TournamentRegistrationResponse.builder()
                .id(reg.getId())
                .tournamentId(reg.getTournament().getId())
                .tournamentName(reg.getTournament().getName())
                .playerId(reg.getPlayer().getId())
                .playerNickname(reg.getPlayer().getNickname())
                .playerAvatarUrl(reg.getPlayer().getAvatar())
                .partnerId(reg.getPartner() != null ? reg.getPartner().getId() : null)
                .partnerNickname(reg.getPartner() != null ? reg.getPartner().getNickname() : null)
                .partnerAvatarUrl(reg.getPartner() != null ? reg.getPartner().getAvatar() : null)
                .status(reg.getStatus())
                .seed(reg.getSeed())
                .strengthScore(reg.getStrengthScore())
                .createdAt(reg.getCreatedAt() != null ? reg.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(reg.getUpdatedAt() != null ? reg.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }
}
