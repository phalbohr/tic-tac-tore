package com.tictactore.controller;

import com.tictactore.dto.PlayerInsightsResponse;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Statistical Insights", description = "Endpoints for retrieving automated, personalized player gameplay insights")
public class InsightController {

    private final InsightService insightService;
    private final UserRepository userRepository;

    @GetMapping("/api/v1/players/{id}/insights")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get player statistical insights", description = "Retrieves up to 5 prioritized, non-judgmental gameplay insights for a given player.")
    @ApiResponse(responseCode = "200", description = "Insights retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Player not found")
    public ResponseEntity<PlayerInsightsResponse> getPlayerInsights(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal Object principal
    ) {
        if (isAnonymous(principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(insightService.getPlayerInsights(id));
    }

    @GetMapping("/api/v1/statistics/insights")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user statistical insights", description = "Retrieves up to 5 prioritized gameplay insights for the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "Insights retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<PlayerInsightsResponse> getCurrentUserInsights(
            @AuthenticationPrincipal Object principal
    ) {
        if (isAnonymous(principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userId = resolveUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(insightService.getPlayerInsights(userId));
    }

    private boolean isAnonymous(Object principal) {
        return principal == null || "anonymousUser".equalsIgnoreCase(principal.toString());
    }

    private UUID resolveUserId(Object principal) {
        if (principal instanceof User user && user.getId() != null) {
            return user.getId();
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .map(User::getId)
                    .orElseGet(() -> tryParseOrGenerateUuid(userDetails.getUsername()));
        }
        if (principal instanceof String str && !"anonymousUser".equalsIgnoreCase(str)) {
            return userRepository.findByEmail(str)
                    .map(User::getId)
                    .orElseGet(() -> tryParseOrGenerateUuid(str));
        }
        return null;
    }

    private UUID tryParseOrGenerateUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception ignored) {
            return UUID.nameUUIDFromBytes(value.getBytes());
        }
    }
}
