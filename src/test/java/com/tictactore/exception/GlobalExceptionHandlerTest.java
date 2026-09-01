package com.tictactore.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("[P1] Should return HTTP 429 with ApiError containing RATE_LIMIT_EXCEEDED code and retryAfter details")
    void shouldReturn429WithRateLimitExceededError() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletWebRequest(request));

        RateLimitExceededException ex = new RateLimitExceededException(3600, "Too many submissions");

        ResponseEntity<ApiError> response = handler.handleRateLimitExceeded(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("RATE_LIMIT_EXCEEDED");
        assertThat(response.getBody().message()).isEqualTo("Too many submissions");
        assertThat(response.getBody().details()).containsEntry("retryAfter", 3600);
    }

    @Test
    @DisplayName("[P1] Should return HTTP 503 with ApiError containing RATE_LIMIT_UNAVAILABLE code and retryAfter=0 when redisFailure is true")
    void shouldReturn503WithRateLimitUnavailableWhenRedisFailure() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletWebRequest(request));

        RateLimitExceededException ex = new RateLimitExceededException("Redis unavailable", new RuntimeException("Redis down"));

        ResponseEntity<ApiError> response = handler.handleRateLimitExceeded(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("RATE_LIMIT_UNAVAILABLE");
        assertThat(response.getBody().message()).contains("Redis unavailable");
        assertThat(response.getBody().details()).containsEntry("retryAfter", 0);
    }

    @Test
    void shouldReturn409ForTournamentConflictException() {
        var ex = new TournamentConflictException("Tournament capacity reached");

        var response = handler.handlePoolConflict(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "Tournament capacity reached");
    }
}
