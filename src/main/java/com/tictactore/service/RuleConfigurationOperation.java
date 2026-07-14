package com.tictactore.service;

import com.tictactore.annotation.Idempotent;
import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.repository.RuleConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RuleConfigurationOperation {

    private final RuleConfigurationRepository repository;

    @Idempotent
    @Transactional(propagation = Propagation.REQUIRED)
    public RuleConfiguration createCustomRule(RuleConfigurationRequest request, UUID createdBy) {
        RuleConfiguration rule = new RuleConfiguration();
        rule.setName(request.name());
        rule.setType(RuleConfigurationType.CUSTOM);
        rule.setGoalLimit(request.goalLimit());
        rule.setGameLimit(request.gameLimit());
        rule.setWinByTwo(request.winByTwo());
        rule.setCreatedBy(createdBy);
        return repository.save(rule);
    }
}
