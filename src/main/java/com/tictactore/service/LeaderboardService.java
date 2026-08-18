package com.tictactore.service;

import com.tictactore.dto.LeaderboardEntry;
import com.tictactore.dto.PageResponse;
import com.tictactore.dto.PlayerStatsResponse;

import java.util.UUID;

public interface LeaderboardService {
    PageResponse<LeaderboardEntry> getLeaderboard(String type, String period, int minMatches, String matchType, String matchFormat, int page, int size);
    PlayerStatsResponse getPersonalStats(UUID userId);
}
