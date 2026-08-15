package com.tictactore.service;

import com.tictactore.dto.LeaderboardEntry;
import com.tictactore.dto.PageResponse;

public interface LeaderboardService {
    PageResponse<LeaderboardEntry> getLeaderboard(String type, String period, int minMatches, String matchType, String matchFormat, int page, int size);
}
