package com.tictactore.controller;

import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.dto.RuleConfigurationResponse;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.model.User;
import com.tictactore.service.RuleConfigurationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rule-configurations")
@RequiredArgsConstructor
public class RuleConfigurationController {

    private final RuleConfigurationService service;

    @GetMapping
    public ResponseEntity<List<RuleConfigurationResponse>> getRuleConfigurations(
            @AuthenticationPrincipal User principal,
            @RequestParam(required = false) RuleConfigurationType type
    ) {
        var rules = service.getAvailableRules(principal.getId(), type);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleConfigurationResponse> getRuleConfigurationById(
            @AuthenticationPrincipal User principal,
            @PathVariable UUID id
    ) {
        var rule = service.getRuleById(principal.getId(), id);
        return ResponseEntity.ok(rule);
    }

    @PostMapping
    public ResponseEntity<RuleConfigurationResponse> createRuleConfiguration(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody RuleConfigurationRequest request
    ) {
        var response = service.createCustomRule(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRuleConfiguration(
            @AuthenticationPrincipal User principal,
            @PathVariable UUID id
    ) {
        service.deleteCustomRule(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
