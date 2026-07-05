package com.tictactore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserMatchController {

    @GetMapping("/preferences/last-rule-system")
    public ResponseEntity<UserPreferencesDto> getLastRuleSystem() {
        return ResponseEntity.ok(new UserPreferencesDto("STANDARD"));
    }

    @GetMapping("/frequent-opponents")
    public ResponseEntity<java.util.List<PlayerDto>> getFrequentOpponents() {
        return ResponseEntity.ok(java.util.List.of(
                new PlayerDto(java.util.UUID.randomUUID().toString(), "Mock Player 1", "avatar1"),
                new PlayerDto(java.util.UUID.randomUUID().toString(), "Mock Player 2", "avatar2")
        ));
    }

    public record UserPreferencesDto(String lastRuleSystem) {}
    public record PlayerDto(String id, String nickname, String avatar) {}
}
