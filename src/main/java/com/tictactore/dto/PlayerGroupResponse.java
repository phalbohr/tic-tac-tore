package com.tictactore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PlayerGroupResponse(
        UUID id,
        String name,
        @JsonProperty("isFavorite")
        boolean isFavorite,
        UUID creatorId,
        List<PlayerSummaryDto> members,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
