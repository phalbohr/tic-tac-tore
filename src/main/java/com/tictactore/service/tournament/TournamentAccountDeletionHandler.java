package com.tictactore.service.tournament;

import java.util.UUID;

public interface TournamentAccountDeletionHandler {

    void handleUserDeletion(UUID userId);
}
