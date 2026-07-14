package com.tictactore.dto;

import com.tictactore.model.RuleConfigurationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RuleConfigurationRequest(
    @NotBlank
    @Size(max = 255)
    String name,

    @NotNull
    RuleConfigurationType type,

    @Min(1)
    @Max(100)
    int goalLimit,

    @Min(1)
    @Max(100)
    int gameLimit,

    boolean winByTwo
) {}
