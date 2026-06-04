package com.tictactore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Test Authentication", description = "Operations for E2E/test authentication bypass")
public interface TestAuthApi {

    @Operation(summary = "Test login for E2E testing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged in test user")
    })
    @org.springframework.web.bind.annotation.GetMapping("/test-login")
    ResponseEntity<Void> testLogin(
            @org.springframework.web.bind.annotation.RequestParam String email,
            @org.springframework.web.bind.annotation.RequestParam String nickname,
            HttpServletRequest request,
            HttpServletResponse response
    );
}
