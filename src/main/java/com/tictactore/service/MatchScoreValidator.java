package com.tictactore.service;

import com.tictactore.dto.GameDto;
import com.tictactore.model.RuleConfiguration;
import java.util.List;

public interface MatchScoreValidator {
    void validateScores(RuleConfiguration ruleConfig, List<GameDto> games);
}
