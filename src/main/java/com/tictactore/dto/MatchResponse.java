package com.tictactore.dto;

import com.tictactore.model.MatchStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Record for returning Match details in API responses.
 */
@Builder
public record MatchResponse(
    UUID id,
    String creatorName,
    String teamAAttackerName,
    String teamADefenderName,
    String teamBAttackerName,
    String teamBDefenderName,
    MatchStatus status,
    List<GameResponse> games,
    LocalDateTime createdAt
) {}
