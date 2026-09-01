package com.tictactore.service;

import com.tictactore.dto.MyRegistrationStatusResponse;
import com.tictactore.dto.RegisterTournamentRequest;
import com.tictactore.dto.TournamentRegistrationResponse;
import com.tictactore.model.RegistrationStatus;

import java.util.List;
import java.util.UUID;

public interface TournamentRegistrationService {

    TournamentRegistrationResponse register(UUID tournamentId, UUID playerId, RegisterTournamentRequest request);

    TournamentRegistrationResponse acceptInvitation(UUID tournamentId, UUID registrationId, UUID partnerId);

    TournamentRegistrationResponse declineInvitation(UUID tournamentId, UUID registrationId, UUID partnerId);

    void cancelRegistration(UUID tournamentId, UUID registrationId, UUID userId);

    List<TournamentRegistrationResponse> listRegistrations(UUID tournamentId, RegistrationStatus status);

    MyRegistrationStatusResponse getMyRegistrationStatus(UUID tournamentId, UUID userId);

    List<TournamentRegistrationResponse> getPendingInvitations(UUID userId);
}
