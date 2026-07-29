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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
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

    @Nested
    @DisplayName("POST /api/v1/matches/{id}/confirm Specs")
    class PostMatchConfirmSpecs {

        @BeforeEach
        void setUpContext() {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("[P0] Should return 200 OK when match confirmation succeeds")
        void shouldReturn200OnSuccessfulConfirmation() throws Exception {
            var matchId = UUID.randomUUID();
            var user = com.tictactore.model.User.builder().id(p2).build();
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user, null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            var response = new MatchResponse(
                    matchId, "key-1", p1, p1, null, p2, null,
                    "CONFIRMED", List.of(), Instant.now(), p2, Instant.now()
            );

            when(matchService.confirmMatch(eq(matchId), eq(p2), eq("idem-key-1"))).thenReturn(response);

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/confirm")
                            .header("Idempotency-Key", "idem-key-1")
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.confirmedByUserId").value(p2.toString()));
        }

        @Test
        @DisplayName("[P1] Should return 403 Forbidden when unauthorized user attempts confirmation")
        void shouldReturn403OnUnauthorizedUser() throws Exception {
            var matchId = UUID.randomUUID();
            var user = com.tictactore.model.User.builder().id(p1).build();
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user, null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            when(matchService.confirmMatch(eq(matchId), eq(p1), any()))
                    .thenThrow(new com.tictactore.exception.UnauthorizedMatchActionException("User is not an opponent"));

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/confirm")
                            .principal(auth))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("User is not an opponent"));
        }

        @Test
        @DisplayName("[P1] Should return 400 Bad Request when match is in invalid state")
        void shouldReturn400OnInvalidState() throws Exception {
            var matchId = UUID.randomUUID();
            var user = com.tictactore.model.User.builder().id(p2).build();
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user, null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            when(matchService.confirmMatch(eq(matchId), eq(p2), any()))
                    .thenThrow(new com.tictactore.exception.InvalidMatchStateException("Match is not in PENDING_APPROVAL status"));

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/confirm")
                            .principal(auth))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Match is not in PENDING_APPROVAL status"));
        }

        @Test
        @DisplayName("[P1] Should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            var matchId = UUID.randomUUID();
            org.springframework.security.core.context.SecurityContextHolder.clearContext();

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/confirm"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("[P0] Should return 200 OK and list of pending matches when authenticated")
        void shouldReturn200AndPendingMatches_whenUserAuthenticated() throws Exception {
            var user = com.tictactore.model.User.builder().id(p1).build();
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user, null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            var response = new MatchResponse(
                    UUID.randomUUID(), "key-1", p2, p2, null, p1, null,
                    "PENDING_APPROVAL", java.util.List.of(new GameDto(10, 5)), java.time.Instant.now()
            );
            var pendingResponse = new com.tictactore.dto.PendingMatchesResponse(1, java.util.List.of(response));
            when(matchService.getPendingMatches(p1)).thenReturn(pendingResponse);

            mockMvc.perform(get("/api/v1/matches/pending").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(1))
                    .andExpect(jsonPath("$.matches[0].status").value("PENDING_APPROVAL"));
        }
    }
}
