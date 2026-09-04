package com.tictactore.model;

import com.tictactore.exception.InvalidMatchStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Match Entity System Auto-Confirmation ATDD Unit Tests")
class MatchTest {

    @Test
    @DisplayName("[P0] Should transition status to CONFIRMED and clear cooldown when status is PENDING_APPROVAL")
    void shouldTransitionToConfirmed_whenStatusIsPendingApproval() {
        var match = Match.builder()
                .id(UUID.randomUUID())
                .creatorId(UUID.randomUUID())
                .opponentId(UUID.randomUUID())
                .status("PENDING_APPROVAL")
                .cooldownExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .createdAt(Instant.now().minus(49, ChronoUnit.HOURS))
                .build();
        var beforeAction = Instant.now();

        match.autoConfirmBySystem();

        assertThat(match.getStatus()).isEqualTo("CONFIRMED");
        assertThat(match.getCooldownExpiresAt()).isNull();
        assertThat(match.getConfirmedAt()).isNotNull();
        assertThat(match.getConfirmedAt()).isAfterOrEqualTo(beforeAction);
    }

    @Test
    @DisplayName("[P0] Should transition status to CONFIRMED and clear cooldown when status is PARTIALLY_CONFIRMED")
    void shouldTransitionToConfirmed_whenStatusIsPartiallyConfirmed() {
        var match = Match.builder()
                .id(UUID.randomUUID())
                .creatorId(UUID.randomUUID())
                .opponentId(UUID.randomUUID())
                .status("PARTIALLY_CONFIRMED")
                .cooldownExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .createdAt(Instant.now().minus(49, ChronoUnit.HOURS))
                .build();
        var beforeAction = Instant.now();

        match.autoConfirmBySystem();

        assertThat(match.getStatus()).isEqualTo("CONFIRMED");
        assertThat(match.getCooldownExpiresAt()).isNull();
        assertThat(match.getConfirmedAt()).isNotNull();
        assertThat(match.getConfirmedAt()).isAfterOrEqualTo(beforeAction);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONFIRMED", "REJECTED", "CANCELLED", "COMPLETED"})
    @DisplayName("[P1] Should throw InvalidMatchStateException when match is not pending confirmation")
    void shouldThrowException_whenStatusIsNotPendingOrPartiallyConfirmed(String invalidStatus) {
        var match = Match.builder()
                .id(UUID.randomUUID())
                .creatorId(UUID.randomUUID())
                .opponentId(UUID.randomUUID())
                .status(invalidStatus)
                .build();

        assertThatThrownBy(match::autoConfirmBySystem)
                .isInstanceOf(InvalidMatchStateException.class)
                .hasMessageContaining("Cannot auto-confirm match in status " + invalidStatus);
    }
}
