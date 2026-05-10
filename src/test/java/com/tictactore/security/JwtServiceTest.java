package com.tictactore.security;

import com.tictactore.model.User;
import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.redisson.api.RedissonClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private static final String EMAIL_TEST = "test@example.com";
    private static final String NAME_TEST = "Test User";
    private static final String INVALID_TOKEN = "invalid.token.value";
    private static final String JWT_PARTS_REGEX = "\\.";
    private static final int JWT_PARTS_COUNT = 3;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private RedissonClient redissonClient;
    
    @MockBean
    private TokenRevocationService tokenRevocationService;

    @Test
    @DisplayName("Generate Token - should return non-blank valid JWT token structure")
    void generateToken_returnsNonBlankToken() {
        var user = User.builder()
                .id(UUID.randomUUID())
                .email(EMAIL_TEST)
                .name(NAME_TEST)
                .build();

        var token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(token.split(JWT_PARTS_REGEX)).hasSize(JWT_PARTS_COUNT);
    }

    @Test
    @DisplayName("Valid Token - should return true for internally generated token")
    void isTokenValid_returnsTrueForValidToken() {
        var user = User.builder()
                .id(UUID.randomUUID())
                .email(EMAIL_TEST)
                .name(NAME_TEST)
                .build();

        var token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("Invalid Token - should return false for malformed token string")
    void isTokenValid_returnsFalseForInvalidToken() {
        assertThat(jwtService.isTokenValid(INVALID_TOKEN)).isFalse();
    }

    @Test
    @DisplayName("Extract User ID - should correctly parse subject from token")
    void extractUserId_returnsCorrectId() {
        var userId = UUID.randomUUID();
        var user = User.builder()
                .id(userId)
                .email(EMAIL_TEST)
                .name(NAME_TEST)
                .build();

        var token = jwtService.generateToken(user);

        assertThat(jwtService.extractUserId(token)).isEqualTo(userId.toString());
    }
}
