package com.tictactore.controller;

import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.dto.RuleConfigurationResponse;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.model.User;
import com.tictactore.service.RuleConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rule-configurations")
@RequiredArgsConstructor
public class RuleConfigurationController {

    private final RuleConfigurationService service;

    @GetMapping
    public ResponseEntity<List<RuleConfigurationResponse>> getRuleConfigurations(
            @RequestParam(required = false) RuleConfigurationType type
    ) {
        if (type == RuleConfigurationType.PRESET) {
            var presets = service.getPresets();
            var responses = presets.stream()
                    .map(config -> new RuleConfigurationResponse(
                            config.getId(),
                            config.getName(),
                            config.getType(),
                            config.getGoalLimit(),
                            config.getGameLimit(),
                            config.isWinByTwo(),
                            config.getCreatedBy(),
                            config.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping
    public ResponseEntity<RuleConfigurationResponse> createRuleConfiguration(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @Valid @RequestBody RuleConfigurationRequest request
    ) {
        var config = service.createRuleConfiguration(request, java.util.UUID.fromString(principal.getUsername()));
        var response = new RuleConfigurationResponse(
                config.getId(),
                config.getName(),
                config.getType(),
                config.getGoalLimit(),
                config.getGameLimit(),
                config.isWinByTwo(),
                config.getCreatedBy(),
                config.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
