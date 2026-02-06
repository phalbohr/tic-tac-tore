package com.tictactore.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("JwtService Tests")
public class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("generateToken - should generate a valid JWT")
    public void generateToken_shouldGenerateValidJwt() {
        // Given
        String username = "test@example.com";

        // When
        String token = jwtService.generateToken(username);

        // Then
        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo(username);
    }

    @Test
    @DisplayName("isTokenValid - should return true for valid token")
    public void isTokenValid_shouldReturnTrueForValidToken() {
        // Given
        String username = "test@example.com";
        String token = jwtService.generateToken(username);

        // When
        boolean isValid = jwtService.isTokenValid(token, username);

        // Then
        assertThat(isValid).isTrue();
    }
}
