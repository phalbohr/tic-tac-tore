package com.tictactore.dto.statistics;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Parameters for Leaderboard request.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardParams {
    private LeaderboardType type = LeaderboardType.OVERALL;
    private TimePeriod period = TimePeriod.ALL_TIME;
    private Integer minMatches = 0;
}
