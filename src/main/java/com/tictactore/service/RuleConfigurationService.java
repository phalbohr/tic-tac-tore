package com.tictactore.service;

import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.repository.RuleConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleConfigurationService {

    private final RuleConfigurationOperation operation;
    private final RuleConfigurationRepository repository;

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public RuleConfiguration createRuleConfiguration(RuleConfigurationRequest request, UUID createdBy) {
        return operation.createCustomRule(request, createdBy);
    }

    @Transactional(readOnly = true)
    public List<RuleConfiguration> getPresets() {
        return repository.findByType(RuleConfigurationType.PRESET);
    }
}
