package com.tictactore.controller;

import com.tictactore.dto.NotificationLogDto;
import com.tictactore.dto.PushSubscriptionRequest;
import com.tictactore.model.User;
import com.tictactore.service.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Push Notifications", description = "Web Push subscription management endpoints")
public class NotificationController {

    private final PushNotificationService pushNotificationService;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieves notification history log entries for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "List of notification logs retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<List<NotificationLogDto>> getNotifications(@AuthenticationPrincipal Object principal) {
        UUID userId = resolveUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<NotificationLogDto> notifications = pushNotificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/subscribe")

    @Operation(summary = "Subscribe to Web Push notifications", description = "Stores or updates a Web Push subscription endpoint and cryptographic keys for the authenticated user.")
    @ApiResponse(responseCode = "201", description = "Subscription saved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody PushSubscriptionRequest request
    ) {
        UUID userId = resolveUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        pushNotificationService.subscribe(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe from Web Push notifications", description = "Removes a Web Push subscription endpoint for the authenticated user.")
    @ApiResponse(responseCode = "204", description = "Unsubscribed successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal Object principal,
            @RequestParam("endpoint") String endpoint
    ) {
        UUID userId = resolveUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        pushNotificationService.unsubscribe(userId, endpoint);
        return ResponseEntity.noContent().build();
    }

    private UUID resolveUserId(Object principal) {
        if (principal instanceof User user && user.getId() != null) {
            return user.getId();
        }
        if (principal != null) {
            return UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        return null;
    }
}
