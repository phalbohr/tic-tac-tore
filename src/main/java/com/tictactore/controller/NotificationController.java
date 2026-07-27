package com.tictactore.controller;

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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Push Notifications", description = "Web Push subscription management endpoints")
public class NotificationController {

    private static final UUID FALLBACK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final PushNotificationService pushNotificationService;

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to Web Push notifications", description = "Stores or updates a Web Push subscription endpoint and cryptographic keys for the authenticated user.")
    @ApiResponse(responseCode = "201", description = "Subscription saved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody PushSubscriptionRequest request
    ) {
        UUID userId = principal != null && principal.getId() != null ? principal.getId() : FALLBACK_USER_ID;
        pushNotificationService.subscribe(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe from Web Push notifications", description = "Removes a Web Push subscription endpoint for the authenticated user.")
    @ApiResponse(responseCode = "204", description = "Unsubscribed successfully")
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal User principal,
            @RequestParam("endpoint") String endpoint
    ) {
        UUID userId = principal != null && principal.getId() != null ? principal.getId() : FALLBACK_USER_ID;
        pushNotificationService.unsubscribe(userId, endpoint);
        return ResponseEntity.noContent().build();
    }
}
