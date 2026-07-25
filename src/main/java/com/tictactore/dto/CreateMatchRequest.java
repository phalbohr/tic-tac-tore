package com.tictactore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateMatchRequest(
    String idempotencyKey,
    UUID creatorId,
    @NotNull UUID teamAAttackerId,
    UUID teamADefenderId,
    @NotNull UUID teamBAttackerId,
    UUID teamBDefenderId,
    @NotEmpty List<@Valid GameDto> games
) {}
