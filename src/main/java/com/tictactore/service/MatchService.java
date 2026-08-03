package com.tictactore.service;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.dto.PendingMatchesResponse;
import java.util.UUID;

import java.util.List;
import java.util.UUID;

public interface MatchService {
    MatchResponse createMatch(CreateMatchRequest request);
    PendingMatchesResponse getPendingMatches(UUID currentUserId);
    MatchResponse confirmMatch(UUID matchId, UUID userId, String idempotencyKey);
    MatchResponse rejectMatch(UUID matchId, UUID userId, com.tictactore.dto.MatchRejectionRequest request, String idempotencyKey);
    void deleteMatch(UUID matchId, UUID userId);
}

