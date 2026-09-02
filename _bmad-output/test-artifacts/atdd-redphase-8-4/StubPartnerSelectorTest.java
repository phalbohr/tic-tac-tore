package com.tictactore.service.tournament;

import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.User;
import com.tictactore.service.tournament.impl.StubPartnerSelectorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StubPartnerSelector ATDD Tests (Story 8.4)")
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
        TournamentRegistration deletedReg = createRegistration("reg-deleted", BigDecimal.valueOf(1500));
        TournamentRegistration candidateFar = createRegistration("reg-far", BigDecimal.valueOf(1200));
        TournamentRegistration candidateClose = createRegistration("reg-close", BigDecimal.valueOf(1480));
        TournamentRegistration candidateVeryFar = createRegistration("reg-vfar", BigDecimal.valueOf(1800));

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
        TournamentRegistration deletedReg = createRegistration("reg-deleted", BigDecimal.valueOf(1500));
        TournamentRegistration candidateA = createRegistration("00000000-0000-0000-0000-000000000001", BigDecimal.valueOf(1450));
        TournamentRegistration candidateB = createRegistration("00000000-0000-0000-0000-000000000002", BigDecimal.valueOf(1550));

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
        TournamentRegistration deletedReg = createRegistration("reg-deleted", BigDecimal.valueOf(1500));
        TournamentRegistration matchPartner = createRegistration("reg-partner", BigDecimal.valueOf(1505));
        TournamentRegistration nextBestCandidate = createRegistration("reg-next-best", BigDecimal.valueOf(1520));

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
        TournamentRegistration deletedReg = createRegistration("reg-deleted", BigDecimal.valueOf(1500));
        TournamentRegistration matchPartner = createRegistration("reg-partner", BigDecimal.valueOf(1500));

        assertThatThrownBy(() -> selector.selectStubPartner(
                tournament,
                deletedReg,
                matchPartner.getId(),
                List.of(matchPartner)
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("No eligible stub partner candidates");
    }

    private TournamentRegistration createRegistration(String id, BigDecimal strength) {
        UUID uuid = id.contains("-") && id.length() == 36 ? UUID.fromString(id) : UUID.nameUUIDFromBytes(id.getBytes());
        return TournamentRegistration.builder()
                .id(uuid)
                .tournament(tournament)
                .user(User.builder().id(UUID.randomUUID()).nickname("Player-" + id).build())
                .strengthScore(strength)
                .build();
    }
}
