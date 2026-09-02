package com.tictactore.service.tournament.impl;

import com.tictactore.event.TournamentStubPartnerAssignedEvent;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRegistrationRepository;
import com.tictactore.service.tournament.StubPartnerSelector;
import com.tictactore.service.tournament.TournamentAccountDeletionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TournamentAccountDeletionHandlerImpl implements TournamentAccountDeletionHandler {

    private final TournamentMatchRepository tournamentMatchRepository;
    private final TournamentRegistrationRepository tournamentRegistrationRepository;
    private final StubPartnerSelector stubPartnerSelector;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void handleUserDeletion(UUID userId) {
        if (userId == null) {
            return;
        }

        List<TournamentRegistration> userRegistrations =
                tournamentRegistrationRepository.findByUserIdAndActiveTournament(userId);

        for (TournamentRegistration deletedReg : userRegistrations) {
            Tournament tournament = deletedReg.getTournament();
            if (tournament == null) {
                continue;
            }

            List<TournamentMatch> matches =
                    tournamentMatchRepository.findByAnyParticipantRegistrationId(tournament.getId(), deletedReg.getId());

            List<TournamentRegistration> activeCandidates =
                    tournamentRegistrationRepository.findAllActiveInTournament(tournament.getId());

            for (TournamentMatch match : matches) {
                if (match.getStatus() == TournamentMatchStatus.COMPLETED || match.getStatus() == TournamentMatchStatus.CANCELLED) {
                    continue;
                }

                if (tournament.getMode() == TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS) {
                    handle2v2RandomDeletion(tournament, match, deletedReg, activeCandidates, userId);
                } else {
                    handleTechnicalDefeat(match, deletedReg);
                }
            }
        }
    }

    private void handle2v2RandomDeletion(
            Tournament tournament,
            TournamentMatch match,
            TournamentRegistration deletedReg,
            List<TournamentRegistration> activeCandidates,
            UUID deletedUserId
    ) {
        UUID deletedId = deletedReg.getId();
        TournamentRegistration teammate = null;

        if (isSameRegistration(match.getParticipant1(), deletedId)) {
            teammate = match.getParticipant1Partner();
            TournamentRegistration stub = selectStub(tournament, deletedReg, teammate, activeCandidates);
            match.setParticipant1(stub);
            match.setParticipant1Stub(true);
            saveMatchAndPublishEvent(tournament, match, deletedUserId, teammate, stub);
        } else if (isSameRegistration(match.getParticipant1Partner(), deletedId)) {
            teammate = match.getParticipant1();
            TournamentRegistration stub = selectStub(tournament, deletedReg, teammate, activeCandidates);
            match.setParticipant1Partner(stub);
            match.setParticipant1Stub(true);
            saveMatchAndPublishEvent(tournament, match, deletedUserId, teammate, stub);
        } else if (isSameRegistration(match.getParticipant2(), deletedId)) {
            teammate = match.getParticipant2Partner();
            TournamentRegistration stub = selectStub(tournament, deletedReg, teammate, activeCandidates);
            match.setParticipant2(stub);
            match.setParticipant2Stub(true);
            saveMatchAndPublishEvent(tournament, match, deletedUserId, teammate, stub);
        } else if (isSameRegistration(match.getParticipant2Partner(), deletedId)) {
            teammate = match.getParticipant2();
            TournamentRegistration stub = selectStub(tournament, deletedReg, teammate, activeCandidates);
            match.setParticipant2Partner(stub);
            match.setParticipant2Stub(true);
            saveMatchAndPublishEvent(tournament, match, deletedUserId, teammate, stub);
        }
    }

    private boolean isSameRegistration(TournamentRegistration reg, UUID targetId) {
        return reg != null && Objects.equals(reg.getId(), targetId);
    }

    private TournamentRegistration selectStub(
            Tournament tournament,
            TournamentRegistration deletedReg,
            TournamentRegistration teammate,
            List<TournamentRegistration> activeCandidates
    ) {
        UUID teammateId = (teammate != null) ? teammate.getId() : null;
        return stubPartnerSelector.selectStubPartner(tournament, deletedReg, teammateId, activeCandidates);
    }

    private void saveMatchAndPublishEvent(
            Tournament tournament,
            TournamentMatch match,
            UUID deletedUserId,
            TournamentRegistration teammate,
            TournamentRegistration stub
    ) {
        tournamentMatchRepository.save(match);

        UUID teammateUserId = (teammate != null && teammate.getUser() != null)
                ? teammate.getUser().getId()
                : null;
        UUID stubPartnerUserId = (stub != null && stub.getUser() != null)
                ? stub.getUser().getId()
                : null;

        eventPublisher.publishEvent(new TournamentStubPartnerAssignedEvent(
                tournament.getId(),
                match.getId(),
                deletedUserId,
                teammateUserId,
                stubPartnerUserId
        ));

        log.info("Assigned stub partner {} for deleted user {} in match {} of tournament {}",
                stubPartnerUserId, deletedUserId, match.getId(), tournament.getId());
    }

    private void handleTechnicalDefeat(TournamentMatch match, TournamentRegistration deletedReg) {
        UUID deletedId = deletedReg.getId();
        if (isSameRegistration(match.getParticipant1(), deletedId)) {
            match.setWinner(match.getParticipant2());
            match.setStatus(TournamentMatchStatus.COMPLETED);
            tournamentMatchRepository.save(match);
        } else if (isSameRegistration(match.getParticipant2(), deletedId)) {
            match.setWinner(match.getParticipant1());
            match.setStatus(TournamentMatchStatus.COMPLETED);
            tournamentMatchRepository.save(match);
        }
    }
}
