package com.tictactore.dto;

import com.tictactore.model.MatchFormat;
import com.tictactore.model.PointDistribution;
import com.tictactore.model.PositionSwapRule;
import com.tictactore.model.RestartRule;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.model.SideSwapRule;
import com.tictactore.model.WinByTwoRule;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record RuleConfigurationResponse(
    UUID id,
    String name,
    RuleConfigurationType type,
    MatchFormat matchFormat,
    int goalLimit,
    int gameLimit,
    int gamesToWin,
    WinByTwoRule winByTwoRule,
    Integer absoluteScoreCap,
    int timeoutsPerGame,
    int timeoutDurationSeconds,
    int possessionLimit5BarSeconds,
    int possessionLimitOtherSeconds,
    SideSwapRule sideSwapRule,
    RestartRule restartRule,
    boolean spinningAllowed,
    boolean aerialsAllowed,
    PositionSwapRule positionSwapRule,
    PointDistribution pointDistribution,
    UUID createdBy,
    OffsetDateTime createdAt
) {}
