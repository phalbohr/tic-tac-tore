package com.tictactore.service;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.dto.PendingMatchesResponse;

public interface MatchService {
    MatchResponse createMatch(CreateMatchRequest request);
    PendingMatchesResponse getPendingMatches();
}
