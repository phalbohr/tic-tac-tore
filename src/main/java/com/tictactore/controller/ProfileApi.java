package com.tictactore.controller;

import com.tictactore.dto.ProfileDto;
import com.tictactore.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Profile", description = "User profile operations")
public interface ProfileApi {

    @Operation(summary = "Get current user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved profile"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @org.springframework.web.bind.annotation.GetMapping("/me")
    ResponseEntity<ProfileDto> getMyProfile(@AuthenticationPrincipal User user);

    @Operation(summary = "Update current user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated profile"),
            @ApiResponse(responseCode = "400", description = "Invalid request or nickname cooldown active"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @org.springframework.web.bind.annotation.PatchMapping("/me")
    ResponseEntity<ProfileDto> updateProfile(
            @AuthenticationPrincipal User user,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.tictactore.dto.UpdateProfileRequest request
    );
}
