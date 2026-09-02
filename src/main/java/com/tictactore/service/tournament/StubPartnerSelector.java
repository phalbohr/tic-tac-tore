package com.tictactore.service.tournament;

import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentRegistration;

import java.util.List;
import java.util.UUID;

public interface StubPartnerSelector {

    TournamentRegistration selectStubPartner(
            Tournament tournament,
            TournamentRegistration deletedRegistration,
            UUID matchPartnerRegistrationId,
            List<TournamentRegistration> candidatePool
    );
}
