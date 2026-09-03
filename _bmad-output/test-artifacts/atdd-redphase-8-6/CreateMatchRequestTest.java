package com.tictactore.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreateMatchRequest ATDD Unit Tests")
class CreateMatchRequestTest {

    @Test
    @DisplayName("Should create request with explicit ruleConfigId and tournamentMatchId")
    void shouldCreateRequestWithExplicitRuleConfigId() {
        UUID creatorId = UUID.randomUUID();
        UUID teamAAttackerId = UUID.randomUUID();
        UUID teamBAttackerId = UUID.randomUUID();
        UUID ruleConfigId = UUID.randomUUID();
        UUID tournamentMatchId = UUID.randomUUID();
        List<GameDto> games = List.of(new GameDto(10, 5, null, null, null, null));

        CreateMatchRequest request = new CreateMatchRequest(
                "idemp-key-1",
                creatorId,
                teamAAttackerId,
                null,
                teamBAttackerId,
                null,
                games,
                "MANUAL",
                "1v1",
                tournamentMatchId,
                ruleConfigId
        );

        assertThat(request.idempotencyKey()).isEqualTo("idemp-key-1");
        assertThat(request.creatorId()).isEqualTo(creatorId);
        assertThat(request.teamAAttackerId()).isEqualTo(teamAAttackerId);
        assertThat(request.teamBAttackerId()).isEqualTo(teamBAttackerId);
        assertThat(request.tournamentMatchId()).isEqualTo(tournamentMatchId);
        assertThat(request.ruleConfigId()).isEqualTo(ruleConfigId);
        assertThat(request.games()).hasSize(1);
    }

    @Test
    @DisplayName("Should default ruleConfigId to null in backward-compatible 10-argument constructor")
    void shouldDefaultRuleConfigIdToNullIn10ArgConstructor() {
        UUID creatorId = UUID.randomUUID();
        UUID teamAAttackerId = UUID.randomUUID();
        UUID teamBAttackerId = UUID.randomUUID();
        UUID tournamentMatchId = UUID.randomUUID();
        List<GameDto> games = List.of(new GameDto(10, 8, null, null, null, null));

        CreateMatchRequest request = new CreateMatchRequest(
                "idemp-key-2",
                creatorId,
                teamAAttackerId,
                null,
                teamBAttackerId,
                null,
                games,
                "MANUAL",
                "1v1",
                tournamentMatchId
        );

        assertThat(request.tournamentMatchId()).isEqualTo(tournamentMatchId);
        assertThat(request.ruleConfigId()).isNull();
    }

    @Test
    @DisplayName("Should default tournamentMatchId and ruleConfigId to null in 9-argument constructor")
    void shouldDefaultTournamentMatchIdAndRuleConfigIdToNullIn9ArgConstructor() {
        UUID creatorId = UUID.randomUUID();
        UUID teamAAttackerId = UUID.randomUUID();
        UUID teamBAttackerId = UUID.randomUUID();
        List<GameDto> games = List.of(new GameDto(10, 8, null, null, null, null));

        CreateMatchRequest request = new CreateMatchRequest(
                "idemp-key-3",
                creatorId,
                teamAAttackerId,
                null,
                teamBAttackerId,
                null,
                games,
                "MANUAL",
                "1v1"
        );

        assertThat(request.tournamentMatchId()).isNull();
        assertThat(request.ruleConfigId()).isNull();
    }
}
