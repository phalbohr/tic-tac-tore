package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tictactore.dto.ChallengeActionResponse;
import com.tictactore.dto.ChallengeResponse;
import com.tictactore.dto.CreateChallengeRequest;
import com.tictactore.exception.GlobalExceptionHandler;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.ChallengeStatus;
import com.tictactore.model.MatchType;
import com.tictactore.model.User;
import com.tictactore.service.ChallengeService;
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
@DisplayName("ChallengeController ATDD Specifications — Match Challenges (Story 6.6)")
class ChallengeControllerATDDTest {

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
        currentUser = User.builder()
                .id(currentUserId)
                .email("challenger@example.com")
                .nickname("ChallengerPro")
                .avatarUrl("https://example.com/avatar.png")
                .build();
        auth = new UsernamePasswordAuthenticationToken(
                currentUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("POST /api/v1/challenges — Create Challenge (AC1)")
    class CreateChallengeTests {

        @Test
        @DisplayName("Should create 1v1 challenge successfully and return 201 Created")
        void shouldCreate1v1Challenge_whenValid() throws Exception {
            var targetUserId = UUID.randomUUID();
            var challengeId = UUID.randomUUID();
            var request = new CreateChallengeRequest(targetUserId, null, MatchType.ONE_VS_ONE, null, "Rematch ready!");
            var response = new ChallengeResponse(
                    challengeId,
                    currentUserId,
                    "ChallengerPro",
                    "https://example.com/avatar.png",
                    targetUserId,
                    "TargetPlayer",
                    "https://example.com/target.png",
                    null,
                    null,
                    MatchType.ONE_VS_ONE,
                    null,
                    null,
                    "Rematch ready!",
                    ChallengeStatus.PENDING,
                    Instant.now(),
                    Instant.now().plusSeconds(86400)
            );
            when(challengeService.createChallenge(eq(currentUserId), any(CreateChallengeRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/challenges")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(challengeId.toString()))
                    .andExpect(jsonPath("$.challengerId").value(currentUserId.toString()))
                    .andExpect(jsonPath("$.targetPlayerId").value(targetUserId.toString()))
                    .andExpect(jsonPath("$.matchType").value("ONE_VS_ONE"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.message").value("Rematch ready!"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when self-challenging")
        void shouldReturn400_whenChallengingSelf() throws Exception {
            var request = new CreateChallengeRequest(currentUserId, null, MatchType.ONE_VS_ONE, null, "Self challenge");
            when(challengeService.createChallenge(eq(currentUserId), any(CreateChallengeRequest.class)))
                    .thenThrow(new IllegalArgumentException("Challenger cannot challenge themselves"));

            mockMvc.perform(post("/api/v1/challenges")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 Conflict when active pending challenge already exists")
        void shouldReturn409_whenDuplicatePendingChallenge() throws Exception {
            var targetUserId = UUID.randomUUID();
            var request = new CreateChallengeRequest(targetUserId, null, MatchType.ONE_VS_ONE, null, "Duplicate");
            when(challengeService.createChallenge(eq(currentUserId), any(CreateChallengeRequest.class)))
                    .thenThrow(new IllegalStateException("An active pending challenge already exists between these parties"));

            mockMvc.perform(post("/api/v1/challenges")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/challenges/incoming & /outgoing — List Challenges (AC2)")
    class ListChallengesTests {

        @Test
        @DisplayName("Should return list of incoming pending challenges")
        void shouldReturnIncomingChallenges() throws Exception {
            var challengeId = UUID.randomUUID();
            var senderId = UUID.randomUUID();
            var response = new ChallengeResponse(
                    challengeId,
                    senderId,
                    "SenderNick",
                    "https://example.com/sender.png",
                    currentUserId,
                    "ChallengerPro",
                    "https://example.com/avatar.png",
                    null,
                    null,
                    MatchType.ONE_VS_ONE,
                    null,
                    null,
                    "Let's play!",
                    ChallengeStatus.PENDING,
                    Instant.now(),
                    Instant.now().plusSeconds(86400)
            );
            when(challengeService.getIncomingChallenges(currentUserId)).thenReturn(List.of(response));

            mockMvc.perform(get("/api/v1/challenges/incoming").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(challengeId.toString()))
                    .andExpect(jsonPath("$[0].challengerNickname").value("SenderNick"))
                    .andExpect(jsonPath("$[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("Should return list of outgoing pending challenges")
        void shouldReturnOutgoingChallenges() throws Exception {
            var challengeId = UUID.randomUUID();
            var targetUserId = UUID.randomUUID();
            var response = new ChallengeResponse(
                    challengeId,
                    currentUserId,
                    "ChallengerPro",
                    "https://example.com/avatar.png",
                    targetUserId,
                    "TargetNick",
                    "https://example.com/target.png",
                    null,
                    null,
                    MatchType.TWO_VS_TWO,
                    null,
                    null,
                    "2v2 match?",
                    ChallengeStatus.PENDING,
                    Instant.now(),
                    Instant.now().plusSeconds(86400)
            );
            when(challengeService.getOutgoingChallenges(currentUserId)).thenReturn(List.of(response));

            mockMvc.perform(get("/api/v1/challenges/outgoing").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(challengeId.toString()))
                    .andExpect(jsonPath("$[0].matchType").value("TWO_VS_TWO"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/challenges/{id}/accept — Accept Challenge (AC3)")
    class AcceptChallengeTests {

        @Test
        @DisplayName("Should accept challenge and return 200 OK")
        void shouldAcceptChallenge_whenAuthorized() throws Exception {
            var challengeId = UUID.randomUUID();
            var actionResponse = new ChallengeActionResponse(challengeId, ChallengeStatus.ACCEPTED, "Challenge accepted successfully");
            when(challengeService.acceptChallenge(challengeId, currentUserId)).thenReturn(actionResponse);

            mockMvc.perform(post("/api/v1/challenges/{id}/accept", challengeId).principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.challengeId").value(challengeId.toString()))
                    .andExpect(jsonPath("$.status").value("ACCEPTED"));
        }

        @Test
        @DisplayName("Should return 403 Forbidden when unauthorized user tries to accept")
        void shouldReturn403_whenUserNotTargetOrGroupMember() throws Exception {
            var challengeId = UUID.randomUUID();
            when(challengeService.acceptChallenge(challengeId, currentUserId))
                    .thenThrow(new AccessDeniedException("User is not authorized to accept this challenge"));

            mockMvc.perform(post("/api/v1/challenges/{id}/accept", challengeId).principal(auth))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 409 Conflict when accepting non-pending challenge")
        void shouldReturn409_whenChallengeNotPending() throws Exception {
            var challengeId = UUID.randomUUID();
            when(challengeService.acceptChallenge(challengeId, currentUserId))
                    .thenThrow(new IllegalStateException("Challenge is not in PENDING status"));

            mockMvc.perform(post("/api/v1/challenges/{id}/accept", challengeId).principal(auth))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/challenges/{id}/decline & /cancel — Decline / Cancel (AC4)")
    class DeclineAndCancelTests {

        @Test
        @DisplayName("Should decline challenge and return 200 OK")
        void shouldDeclineChallenge_whenTarget() throws Exception {
            var challengeId = UUID.randomUUID();
            var actionResponse = new ChallengeActionResponse(challengeId, ChallengeStatus.DECLINED, "Challenge declined");
            when(challengeService.declineChallenge(challengeId, currentUserId)).thenReturn(actionResponse);

            mockMvc.perform(post("/api/v1/challenges/{id}/decline", challengeId).principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DECLINED"));
        }

        @Test
        @DisplayName("Should cancel challenge and return 200 OK when challenger")
        void shouldCancelChallenge_whenChallenger() throws Exception {
            var challengeId = UUID.randomUUID();
            var actionResponse = new ChallengeActionResponse(challengeId, ChallengeStatus.CANCELLED, "Challenge cancelled");
            when(challengeService.cancelChallenge(challengeId, currentUserId)).thenReturn(actionResponse);

            mockMvc.perform(post("/api/v1/challenges/{id}/cancel", challengeId).principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("Should return 404 Not Found when challenge does not exist")
        void shouldReturn404_whenChallengeNotFound() throws Exception {
            var nonExistentId = UUID.randomUUID();
            when(challengeService.cancelChallenge(nonExistentId, currentUserId))
                    .thenThrow(new ResourceNotFoundException("Match challenge not found: " + nonExistentId));

            mockMvc.perform(post("/api/v1/challenges/{id}/cancel", nonExistentId).principal(auth))
                    .andExpect(status().isNotFound());
        }
    }
}
