package com.tictactore.dto;

import com.tictactore.model.TournamentMatchStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TournamentMatchResponse ATDD Unit Tests")
class TournamentMatchResponseTest {

    @Test
    @DisplayName("Should contain ruleConfigurationId and ruleConfigurationName")
    void shouldContainRuleConfigurationFields() {
        UUID id = UUID.randomUUID();
        UUID tournamentId = UUID.randomUUID();
        UUID ruleConfigId = UUID.randomUUID();
        String ruleConfigName = "Official 3-Game Standard";

        TournamentMatchResponse response = TournamentMatchResponse.builder()
                .id(id)
                .tournamentId(tournamentId)
                .round(1)
                .matchOrder(1)
                .status(TournamentMatchStatus.READY)
                .isAvailable(true)
                .isOpponentBusy(false)
                .busyParticipantNicknames(List.of())
                .ruleConfigurationId(ruleConfigId)
                .ruleConfigurationName(ruleConfigName)
                .createdAt(Instant.now())
                .build();

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.tournamentId()).isEqualTo(tournamentId);
        assertThat(response.ruleConfigurationId()).isEqualTo(ruleConfigId);
        assertThat(response.ruleConfigurationName()).isEqualTo(ruleConfigName);
    }
}
