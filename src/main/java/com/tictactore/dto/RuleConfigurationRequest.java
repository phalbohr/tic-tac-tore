package com.tictactore.dto;

import com.tictactore.model.MatchFormat;
import com.tictactore.model.PointDistribution;
import com.tictactore.model.PositionSwapRule;
import com.tictactore.model.RestartRule;
import com.tictactore.model.SideSwapRule;
import com.tictactore.model.WinByTwoRule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RuleConfigurationRequest(
    @NotBlank
    @Size(max = 50)
    String name,

    @NotNull
    MatchFormat matchFormat,

    @Min(1)
    @Max(100)
    int goalLimit,

    @Min(1)
    @Max(15)
    int gameLimit,

    @Min(1)
    @Max(15)
    int gamesToWin,

    @NotNull
    WinByTwoRule winByTwoRule,

    @Min(1)
    @Max(100)
    Integer absoluteScoreCap,

    @Min(0)
    @Max(10)
    int timeoutsPerGame,

    @Min(0)
    @Max(300)
    int timeoutDurationSeconds,

    @Min(0)
    @Max(60)
    int possessionLimit5BarSeconds,

    @Min(0)
    @Max(60)
    int possessionLimitOtherSeconds,

    @NotNull
    SideSwapRule sideSwapRule,

    @NotNull
    RestartRule restartRule,

    boolean spinningAllowed,

    boolean aerialsAllowed,

    @NotNull
    PositionSwapRule positionSwapRule,

    @NotNull
    PointDistribution pointDistribution
) {}
