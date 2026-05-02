package com.tictactore.security;

import com.tictactore.model.User;
import com.tictactore.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generateToken_returnsNonBlankToken() {
        var user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .build();

        var token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void isTokenValid_returnsTrueForValidToken() {
        var user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .build();

        var token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForInvalidToken() {
        assertThat(jwtService.isTokenValid("invalid.token.value")).isFalse();
    }

    @Test
    void extractUserId_returnsCorrectId() {
        var userId = UUID.randomUUID();
        var user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .build();

        var token = jwtService.generateToken(user);

        assertThat(jwtService.extractUserId(token)).isEqualTo(userId.toString());
    }
}
