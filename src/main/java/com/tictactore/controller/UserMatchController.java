package com.tictactore.controller;

import com.tictactore.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserMatchController {

    private final UserService userService;

    public UserMatchController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/preferences/last-rule-system")
    public ResponseEntity<UserPreferencesDto> getLastRuleSystem() {
        return ResponseEntity.ok(userService.getLastRuleSystem());
    }

    @GetMapping("/frequent-opponents")
    public ResponseEntity<java.util.List<PlayerDto>> getFrequentOpponents() {
        return ResponseEntity.ok(userService.getFrequentOpponents());
    }

    @GetMapping("/players/search")
    public ResponseEntity<java.util.List<PlayerDto>> searchPlayers(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(java.util.List.of());
        }
        return ResponseEntity.ok(userService.searchActiveUsers(q));
    }

    public record UserPreferencesDto(String lastRuleSystem) {}
    public record PlayerDto(String id, String nickname, String avatar) {}
}
