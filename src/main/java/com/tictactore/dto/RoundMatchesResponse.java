package com.tictactore.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RoundMatchesResponse(
        int round,
        String roundName,
        List<TournamentMatchResponse> matches
) {}
