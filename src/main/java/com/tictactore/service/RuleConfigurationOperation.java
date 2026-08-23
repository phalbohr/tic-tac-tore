package com.tictactore.service;

import com.tictactore.annotation.Idempotent;
import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.repository.RuleConfigurationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RuleConfigurationOperation {

    private final RuleConfigurationRepository repository;

    @Idempotent
    @Transactional(propagation = Propagation.REQUIRED)
    public RuleConfiguration createCustomRule(RuleConfigurationRequest request, UUID createdBy) {
        RuleConfiguration rule = RuleConfiguration.builder()
                .name(request.name().trim())
                .type(RuleConfigurationType.CUSTOM)
                .goalLimit(request.goalLimit())
                .gameLimit(request.gameLimit())
                .winByTwo(request.winByTwo())
                .absoluteScoreCap(request.absoluteScoreCap())
                .timeoutsPerGame(request.timeoutsPerGame())
                .timeoutDurationSeconds(request.timeoutDurationSeconds())
                .possessionLimit5BarSeconds(request.possessionLimit5BarSeconds())
                .possessionLimitOtherSeconds(request.possessionLimitOtherSeconds())
                .sideSwapRule(request.sideSwapRule())
                .restartRule(request.restartRule())
                .spinningAllowed(request.spinningAllowed())
                .aerialsAllowed(request.aerialsAllowed())
                .positionSwapRule(request.positionSwapRule())
                .pointDistribution(request.pointDistribution())
                .createdBy(createdBy)
                .build();
        return repository.save(rule);
    }

    @Idempotent
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteCustomRule(UUID id) {
        repository.deleteById(id);
    }
}
