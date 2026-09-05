package com.tictactore.service;

import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.dto.RuleConfigurationResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.repository.RuleConfigurationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RuleConfigurationService {

    public static final int MAX_CUSTOM_TEMPLATES_PER_USER = RuleConfigurationOperation.MAX_CUSTOM_TEMPLATES_PER_USER;

    private final RuleConfigurationOperation operation;
    private final RuleConfigurationRepository repository;

    @Transactional(readOnly = true)
    public List<RuleConfigurationResponse> getAvailableRules(UUID userId, RuleConfigurationType type) {
        List<RuleConfiguration> configs;
        if (type == RuleConfigurationType.PRESET) {
            configs = repository.findByTypeOrderByCreatedAtDesc(RuleConfigurationType.PRESET);
        } else if (type == RuleConfigurationType.CUSTOM) {
            configs = repository.findByCreatedByOrderByCreatedAtDesc(userId);
        } else {
            configs = repository.findByTypeOrCreatedByOrderByCreatedAtDesc(RuleConfigurationType.PRESET, userId);
        }
        return configs.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RuleConfigurationResponse getRuleById(UUID userId, UUID id) {
        RuleConfiguration rule = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule configuration not found"));

        if (rule.getType() != RuleConfigurationType.PRESET && !rule.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("Access denied to foreign rule configuration");
        }

        return toResponse(rule);
    }

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public RuleConfigurationResponse createCustomRule(UUID userId, RuleConfigurationRequest request) {
        RuleConfiguration saved = operation.createCustomRule(request, userId);
        return toResponse(saved);
    }

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void deleteCustomRule(UUID userId, UUID id) {
        RuleConfiguration rule = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule configuration not found"));

        if (rule.getType() == RuleConfigurationType.PRESET) {
            throw new AccessDeniedException("System presets cannot be deleted");
        }
        if (!rule.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("Cannot delete custom rule template owned by another user");
        }

        operation.deleteCustomRule(id);
    }

    public RuleConfigurationResponse toResponse(RuleConfiguration config) {
        return RuleConfigurationResponse.builder()
                .id(config.getId())
                .name(config.getName())
                .type(config.getType())
                .matchFormat(config.getMatchFormat())
                .goalLimit(config.getGoalLimit())
                .gameLimit(config.getGameLimit())
                .gamesToWin(config.getGamesToWin())
                .winByTwoRule(config.getWinByTwoRule())
                .absoluteScoreCap(config.getAbsoluteScoreCap())
                .timeoutsPerGame(config.getTimeoutsPerGame())
                .timeoutDurationSeconds(config.getTimeoutDurationSeconds())
                .possessionLimit5BarSeconds(config.getPossessionLimit5BarSeconds())
                .possessionLimitOtherSeconds(config.getPossessionLimitOtherSeconds())
                .sideSwapRule(config.getSideSwapRule())
                .restartRule(config.getRestartRule())
                .spinningAllowed(config.isSpinningAllowed())
                .aerialsAllowed(config.isAerialsAllowed())
                .positionSwapRule(config.getPositionSwapRule())
                .pointDistribution(config.getPointDistribution())
                .createdBy(config.getCreatedBy())
                .createdAt(config.getCreatedAt())
                .build();
    }
}
