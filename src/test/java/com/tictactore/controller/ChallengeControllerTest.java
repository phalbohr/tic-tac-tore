package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tictactore.dto.ChallengeActionResponse;
import com.tictactore.dto.ChallengeResponse;
import com.tictactore.dto.CreateChallengeRequest;
import com.tictactore.exception.ChallengeConflictException;
import com.tictactore.exception.GlobalExceptionHandler;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.exception.ValidationException;
import com.tictactore.model.ChallengeStatus;
import com.tictactore.model.MatchType;
import com.tictactore.model.User;
import com.tictactore.service.ChallengeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeController Specifications")
class ChallengeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ChallengeService challengeService;

    @InjectMocks
    private ChallengeController challengeController;

    private UUID currentUserId;
    private User currentUser;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(challengeController)
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        currentUserId = UUID.randomUUID();
        currentUser = User.builder().id(currentUserId).email("user@example.com").nickname("CurrentUser").build();
        auth = new UsernamePasswordAuthenticationToken(currentUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("POST /api/v1/challenges Endpoint Specs")
    class CreateChallengeSpecs {

        @Test
        void shouldReturn201WhenChallengeCreatedSuccessfully() throws Exception {
            var targetId = UUID.randomUUID();
            var challengeId = UUID.randomUUID();
            var request = new CreateChallengeRequest(targetId, null, MatchType.ONE_VS_ONE, null, "Ready?");
            var response = new ChallengeResponse(
                    challengeId,
                    currentUserId,
                    "CurrentUser",
                    "avatar-1",
                    targetId,
                    "TargetUser",
                    "avatar-2",
                    null,
                    null,
                    MatchType.ONE_VS_ONE,
                    null,
                    null,
                    "Ready?",
                    ChallengeStatus.PENDING,
                    Instant.now(),
                    Instant.now().plusSeconds(604800)
            );

            when(challengeService.createChallenge(eq(currentUserId), any(CreateChallengeRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/challenges")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(challengeId.toString()))
                    .andExpect(jsonPath("$.challengerId").value(currentUserId.toString()))
                    .andExpect(jsonPath("$.targetPlayerId").value(targetId.toString()))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        void shouldReturn400WhenValidationFails() throws Exception {
            var request = new CreateChallengeRequest(currentUserId, null, MatchType.ONE_VS_ONE, null, null);
            when(challengeService.createChallenge(eq(currentUserId), any(CreateChallengeRequest.class)))
                    .thenThrow(new ValidationException("Challenger cannot challenge themselves"));

            mockMvc.perform(post("/api/v1/challenges")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Challenger cannot challenge themselves"));
        }

        @Test
        void shouldReturn409WhenDuplicateChallengeExists() throws Exception {
            var targetId = UUID.randomUUID();
            var request = new CreateChallengeRequest(targetId, null, MatchType.ONE_VS_ONE, null, null);
            when(challengeService.createChallenge(eq(currentUserId), any(CreateChallengeRequest.class)))
                    .thenThrow(new ChallengeConflictException("An active pending challenge already exists"));

            mockMvc.perform(post("/api/v1/challenges")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("An active pending challenge already exists"));
        }

        @Test
        @WithAnonymousUser
        void shouldReturn401WhenUnauthenticated() throws Exception {
            SecurityContextHolder.clearContext();
            var request = new CreateChallengeRequest(UUID.randomUUID(), null, MatchType.ONE_VS_ONE, null, null);

            mockMvc.perform(post("/api/v1/challenges")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/challenges/incoming & /outgoing Endpoint Specs")
    class QueryEndpointSpecs {

        @Test
        void shouldReturnIncomingChallenges() throws Exception {
            var challengeId = UUID.randomUUID();
            var response = new ChallengeResponse(
                    challengeId,
                    UUID.randomUUID(),
                    "OtherUser",
                    "avatar-2",
                    currentUserId,
                    "CurrentUser",
                    "avatar-1",
                    null,
                    null,
                    MatchType.ONE_VS_ONE,
                    null,
                    null,
                    "Ready?",
                    ChallengeStatus.PENDING,
                    Instant.now(),
                    Instant.now().plusSeconds(604800)
            );

            when(challengeService.getIncomingChallenges(eq(currentUserId))).thenReturn(List.of(response));

            mockMvc.perform(get("/api/v1/challenges/incoming")
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(challengeId.toString()))
                    .andExpect(jsonPath("$[0].status").value("PENDING"));
        }

        @Test
        void shouldReturnOutgoingChallenges() throws Exception {
            var challengeId = UUID.randomUUID();
            var response = new ChallengeResponse(
                    challengeId,
                    currentUserId,
                    "CurrentUser",
                    "avatar-1",
                    UUID.randomUUID(),
                    "TargetUser",
                    "avatar-2",
                    null,
                    null,
                    MatchType.ONE_VS_ONE,
                    null,
                    null,
                    "Ready?",
                    ChallengeStatus.PENDING,
                    Instant.now(),
                    Instant.now().plusSeconds(604800)
            );

            when(challengeService.getOutgoingChallenges(eq(currentUserId))).thenReturn(List.of(response));

            mockMvc.perform(get("/api/v1/challenges/outgoing")
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(challengeId.toString()));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/challenges/{id}/[accept|decline|cancel] Endpoint Specs")
    class ActionEndpointSpecs {

        @Test
        void shouldAcceptChallenge() throws Exception {
            var challengeId = UUID.randomUUID();
            var response = new ChallengeActionResponse(challengeId, ChallengeStatus.ACCEPTED, "Challenge accepted successfully");

            when(challengeService.acceptChallenge(eq(challengeId), eq(currentUserId))).thenReturn(response);

            mockMvc.perform(post("/api/v1/challenges/{id}/accept", challengeId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.challengeId").value(challengeId.toString()))
                    .andExpect(jsonPath("$.status").value("ACCEPTED"));
        }

        @Test
        void shouldDeclineChallenge() throws Exception {
            var challengeId = UUID.randomUUID();
            var response = new ChallengeActionResponse(challengeId, ChallengeStatus.DECLINED, "Challenge declined successfully");

            when(challengeService.declineChallenge(eq(challengeId), eq(currentUserId))).thenReturn(response);

            mockMvc.perform(post("/api/v1/challenges/{id}/decline", challengeId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.challengeId").value(challengeId.toString()))
                    .andExpect(jsonPath("$.status").value("DECLINED"));
        }

        @Test
        void shouldCancelChallenge() throws Exception {
            var challengeId = UUID.randomUUID();
            var response = new ChallengeActionResponse(challengeId, ChallengeStatus.CANCELLED, "Challenge cancelled successfully");

            when(challengeService.cancelChallenge(eq(challengeId), eq(currentUserId))).thenReturn(response);

            mockMvc.perform(post("/api/v1/challenges/{id}/cancel", challengeId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.challengeId").value(challengeId.toString()))
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        void shouldReturn403WhenUnauthorizedAction() throws Exception {
            var challengeId = UUID.randomUUID();
            when(challengeService.acceptChallenge(eq(challengeId), eq(currentUserId)))
                    .thenThrow(new AccessDeniedException("User is not authorized"));

            mockMvc.perform(post("/api/v1/challenges/{id}/accept", challengeId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn404WhenNotFound() throws Exception {
            var challengeId = UUID.randomUUID();
            when(challengeService.getChallengeById(eq(challengeId), eq(currentUserId)))
                    .thenThrow(new ResourceNotFoundException("Challenge not found"));

            mockMvc.perform(get("/api/v1/challenges/{id}", challengeId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
