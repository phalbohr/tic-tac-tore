package com.tictactore.dto.tournament;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TournamentStandingResponse DTO record tests")
class TournamentStandingResponseTest {

    @Test
    @DisplayName("Should construct with full statistics and partner attributes")
    void shouldConstructWithFullStatisticsAndPartnerAttributes() {
        var registrationId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var partnerUserId = UUID.randomUUID();

        var response = new TournamentStandingResponse(
                registrationId,
                userId,
                "Alice",
                "https://example.com/alice.png",
                partnerUserId,
                "Bob",
                "https://example.com/bob.png",
                5,
                4,
                1,
                8,
                3,
                5,
                12,
                false,
                1
        );

        assertThat(response.registrationId()).isEqualTo(registrationId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.nickname()).isEqualTo("Alice");
        assertThat(response.avatarUrl()).isEqualTo("https://example.com/alice.png");
        assertThat(response.partnerUserId()).isEqualTo(partnerUserId);
        assertThat(response.partnerNickname()).isEqualTo("Bob");
        assertThat(response.partnerAvatarUrl()).isEqualTo("https://example.com/bob.png");
        assertThat(response.matchesPlayed()).isEqualTo(5);
        assertThat(response.wins()).isEqualTo(4);
        assertThat(response.losses()).isEqualTo(1);
        assertThat(response.gamesWon()).isEqualTo(8);
        assertThat(response.gamesLost()).isEqualTo(3);
        assertThat(response.gameDifference()).isEqualTo(5);
        assertThat(response.points()).isEqualTo(12);
        assertThat(response.isEliminated()).isFalse();
        assertThat(response.rank()).isEqualTo(1);
    }
}
