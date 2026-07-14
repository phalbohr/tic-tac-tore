package com.tictactore.dto;

import com.tictactore.model.RuleConfigurationType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RuleConfigurationResponse(
    UUID id,
    String name,
    RuleConfigurationType type,
    int goalLimit,
    int gameLimit,
    boolean winByTwo,
    UUID createdBy,
    OffsetDateTime createdAt
) {}
