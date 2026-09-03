package com.tictactore.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TournamentCompletedEvent record tests")
class TournamentCompletedEventTest {

    @Test
    @DisplayName("Should construct with tournament ID, winner registration ID, and completion timestamp")
    void shouldConstructWithTournamentIdWinnerAndTimestamp() {
        var tournamentId = UUID.randomUUID();
        var winnerRegistrationId = UUID.randomUUID();
        var completedAt = Instant.now();

        var event = new TournamentCompletedEvent(tournamentId, winnerRegistrationId, completedAt);

        assertThat(event.tournamentId()).isEqualTo(tournamentId);
        assertThat(event.winnerRegistrationId()).isEqualTo(winnerRegistrationId);
        assertThat(event.completedAt()).isEqualTo(completedAt);
    }
}
