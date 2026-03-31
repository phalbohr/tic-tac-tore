package com.tictactore.dto.statistics;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Parameters for Head-to-Head statistics request.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class H2HParams {
    private TimePeriod period = TimePeriod.ALL_TIME;
    private LeaderboardType myPosition = LeaderboardType.OVERALL;
    private LeaderboardType opponentPosition = LeaderboardType.OVERALL;
}
