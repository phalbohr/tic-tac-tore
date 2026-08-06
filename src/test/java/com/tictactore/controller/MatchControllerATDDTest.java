package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.GameDto;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.GlobalExceptionHandler;
import com.tictactore.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ATDD Red-Phase Scaffolds for Match REST Controller (POST /api/v1/matches).
 * Story 2.4: Match Submission with Undo Window
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchController ATDD Specifications")
class MatchControllerATDDTest {

    private MockMvc mockMvc;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchController matchController;

    private ObjectMapper objectMapper;
    private UUID p1, p2, p3, p4;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(matchController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        p1 = UUID.randomUUID();
        p2 = UUID.randomUUID();
        p3 = UUID.randomUUID();
        p4 = UUID.randomUUID();
    }

    @Nested
    @DisplayName("POST /api/v1/matches Endpoint Specs")
    class PostMatchesSpecs {

        @Test
        @DisplayName("[P0] Should return 201 Created with MatchResponse JSON payload when valid CreateMatchRequest is posted")
        void shouldReturn201CreatedOnValidSubmission() throws Exception {
            var response = new MatchResponse(
                    UUID.randomUUID(), "key-123", p1, p1, p2, p3, p4,
                    "PENDING_APPROVAL", List.of(new GameDto(10, 5)), Instant.now()
            );
            when(matchService.createMatch(any())).thenReturn(response);

            var request = new CreateMatchRequest("key-123", p1, p1, p2, p3, p4, List.of(new GameDto(10, 5)), null, null);

            mockMvc.perform(post("/api/v1/matches")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
        }

        @Test
        @DisplayName("[P1] Should return 400 Bad Request when request body violates @Valid constraints")
        void shouldReturn400OnInvalidBody() throws Exception {
            var request = new CreateMatchRequest("key-123", null, null, p2, p3, p4, List.of(), null, null);

            mockMvc.perform(post("/api/v1/matches")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[P1] Should handle idempotency key gracefully when duplicate request is sent")
        void shouldHandleIdempotencyHeader() throws Exception {
            var response = new MatchResponse(
                    UUID.randomUUID(), "key-123", p1, p1, p2, p3, p4,
                    "PENDING_APPROVAL", List.of(new GameDto(10, 5)), Instant.now()
            );
            when(matchService.createMatch(any())).thenReturn(response);

            var request = new CreateMatchRequest("key-123", p1, p1, p2, p3, p4, List.of(new GameDto(10, 5)), null, null);

            mockMvc.perform(post("/api/v1/matches")
                            .header("Idempotency-Key", "key-123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.idempotencyKey").value("key-123"));
        }
    }
}
