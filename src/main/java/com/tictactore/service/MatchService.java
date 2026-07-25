package com.tictactore.service;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.MatchResponse;

public interface MatchService {
    MatchResponse createMatch(CreateMatchRequest request);
}
