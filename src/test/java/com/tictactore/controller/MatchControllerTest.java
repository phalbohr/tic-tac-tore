package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.GameDto;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.DuplicatePlayerException;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchController Unit Tests")
class MatchControllerTest {

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
    @DisplayName("POST /api/v1/matches Specs")
    class PostMatchesSpecs {

        @Test
        @DisplayName("[P0] Should return 201 Created with MatchResponse JSON payload when valid CreateMatchRequest is posted")
        void shouldReturn201CreatedOnValidSubmission() throws Exception {
            CreateMatchRequest request = new CreateMatchRequest(
                    "key-1", p1, p1, p2, p3, p4,
                    List.of(new GameDto(10, 8))
            );

            MatchResponse response = new MatchResponse(
                    UUID.randomUUID(), "key-1", p1, p1, p2, p3, p4,
                    "PENDING_APPROVAL", List.of(new GameDto(10, 8)), Instant.now()
            );

            when(matchService.createMatch(any(CreateMatchRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/matches")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                    .andExpect(jsonPath("$.idempotencyKey").value("key-1"));
        }

        @Test
        @DisplayName("[P1] Should return 400 Bad Request when duplicate players selected")
        void shouldReturn400OnDuplicatePlayers() throws Exception {
            CreateMatchRequest request = new CreateMatchRequest(
                    "key-2", p1, p1, p1, p3, p4,
                    List.of(new GameDto(10, 8))
            );

            when(matchService.createMatch(any(CreateMatchRequest.class)))
                    .thenThrow(new DuplicatePlayerException("Same player selected in multiple positions"));

            mockMvc.perform(post("/api/v1/matches")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Same player selected in multiple positions"));
        }
    }
}
