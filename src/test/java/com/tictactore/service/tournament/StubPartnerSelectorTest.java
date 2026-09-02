package com.tictactore.service.tournament;

import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.User;
import com.tictactore.service.tournament.impl.StubPartnerSelectorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StubPartnerSelector Tests")
class StubPartnerSelectorTest {

    private StubPartnerSelector selector;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        selector = new StubPartnerSelectorImpl();
        tournament = Tournament.builder()
                .id(UUID.fromString("11111111-2222-3333-4444-555555555555"))
                .name("Random Pairing Cup")
                .build();
    }

    @Test
    void shouldSelectCandidateWithClosestFrozenStrengthScore() {
        TournamentRegistration deletedReg = createRegistration("reg-deleted", 1500.0);
        TournamentRegistration candidateFar = createRegistration("reg-far", 1200.0);
        TournamentRegistration candidateClose = createRegistration("reg-close", 1480.0);
        TournamentRegistration candidateVeryFar = createRegistration("reg-vfar", 1800.0);

        TournamentRegistration selected = selector.selectStubPartner(
                tournament,
                deletedReg,
                null,
                List.of(candidateFar, candidateClose, candidateVeryFar)
        );

        assertThat(selected).isEqualTo(candidateClose);
    }

    @Test
    void shouldBreakTiesDeterministicallyByRegistrationId() {
        TournamentRegistration deletedReg = createRegistration("reg-deleted", 1500.0);
        TournamentRegistration candidateA = createRegistration("00000000-0000-0000-0000-000000000001", 1450.0);
        TournamentRegistration candidateB = createRegistration("00000000-0000-0000-0000-000000000002", 1550.0);

        TournamentRegistration selected = selector.selectStubPartner(
                tournament,
                deletedReg,
                null,
                List.of(candidateB, candidateA)
        );

        assertThat(selected).isEqualTo(candidateA);
    }

    @Test
    void shouldExcludeCurrentMatchPartnerFromSelection() {
        TournamentRegistration deletedReg = createRegistration("reg-deleted", 1500.0);
        TournamentRegistration matchPartner = createRegistration("reg-partner", 1505.0);
        TournamentRegistration nextBestCandidate = createRegistration("reg-next-best", 1520.0);

        TournamentRegistration selected = selector.selectStubPartner(
                tournament,
                deletedReg,
                matchPartner.getId(),
                List.of(matchPartner, nextBestCandidate)
        );

        assertThat(selected).isEqualTo(nextBestCandidate);
    }

    @Test
    void shouldThrowExceptionWhenNoEligibleCandidatesAvailable() {
        TournamentRegistration deletedReg = createRegistration("reg-deleted", 1500.0);
        TournamentRegistration matchPartner = createRegistration("reg-partner", 1500.0);

        assertThatThrownBy(() -> selector.selectStubPartner(
                tournament,
                deletedReg,
                matchPartner.getId(),
                List.of(matchPartner)
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("No eligible stub partner candidates");
    }

    private TournamentRegistration createRegistration(String id, Double strength) {
        UUID uuid = id.contains("-") && id.length() == 36 ? UUID.fromString(id) : UUID.nameUUIDFromBytes(id.getBytes());
        return TournamentRegistration.builder()
                .id(uuid)
                .tournament(tournament)
                .player(User.builder().id(UUID.randomUUID()).nickname("Player-" + id).build())
                .strengthScore(strength)
                .build();
    }
}
