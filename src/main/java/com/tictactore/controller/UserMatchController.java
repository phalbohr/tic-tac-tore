package com.tictactore.controller;

import com.tictactore.service.RateLimitService;
import com.tictactore.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserMatchController {

    private final UserService userService;
    private final RateLimitService rateLimitService;

    public UserMatchController(UserService userService, RateLimitService rateLimitService) {
        this.userService = userService;
        this.rateLimitService = rateLimitService;
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
    public ResponseEntity<java.util.List<PlayerDto>> searchPlayers(
            @RequestParam(required = false) String q,
            HttpServletRequest request) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(java.util.List.of());
        }
        rateLimitService.checkSearchLimit(getClientIp(request));
        return ResponseEntity.ok(userService.searchActiveUsers(q));
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    public record UserPreferencesDto(String lastRuleSystem) {}
    public record PlayerDto(String id, String nickname, String avatar) {}
}
