package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.dto.GameDto;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.GlobalExceptionHandler;
import com.tictactore.exception.InvalidMatchStateException;
import com.tictactore.exception.UnauthorizedMatchActionException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ATDD Red-Phase Scaffolds for Match Confirmation REST Endpoint (POST /api/v1/matches/{id}/confirm).
 * Story 3.2: Single-tap Confirmation with Undo Window
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchConfirmation Controller ATDD Specifications")
class MatchConfirmationControllerATDDTest {

    private MockMvc mockMvc;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchController matchController;

    private ObjectMapper objectMapper;
    private UUID matchId;
    private UUID opponentId;
    private UUID creatorId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(matchController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        matchId = UUID.randomUUID();
        opponentId = UUID.randomUUID();
        creatorId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("POST /api/v1/matches/{id}/confirm Endpoint Specs")
    class PostMatchConfirmSpecs {

        @Test
        @DisplayName("[P0] Should return 200 OK with confirmed MatchResponse JSON when opponent confirms pending match")
        void shouldReturn200OKOnOpponentConfirmation() throws Exception {
            MatchResponse confirmedResponse = new MatchResponse(
                    matchId, "key-conf-1", creatorId, creatorId, UUID.randomUUID(), opponentId, UUID.randomUUID(),
                    "CONFIRMED", List.of(new GameDto(10, 8)), Instant.now()
            );

            when(matchService.confirmMatch(eq(matchId), any(), any())).thenReturn(confirmedResponse);

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/confirm")
                            .header("Idempotency-Key", "key-conf-1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("[P0] Should return 403 Forbidden when unauthorized user or creator attempts confirmation")
        void shouldReturn403OnUnauthorizedConfirmation() throws Exception {
            when(matchService.confirmMatch(eq(matchId), any(), any()))
                    .thenThrow(new UnauthorizedMatchActionException("User is not an opponent"));

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/confirm")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("[P1] Should return 400 Bad Request when match is not in PENDING_APPROVAL status")
        void shouldReturn400OnInvalidMatchState() throws Exception {
            when(matchService.confirmMatch(eq(matchId), any(), any()))
                    .thenThrow(new InvalidMatchStateException("Match is not pending approval"));

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/confirm")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
