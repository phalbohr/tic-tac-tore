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
import com.tictactore.dto.MatchRejectionRequest;
import com.tictactore.dto.PendingMatchesResponse;
import com.tictactore.exception.UnauthorizedMatchActionException;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
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
                    List.of(new GameDto(10, 8)),
                    null, null
            );

            MatchResponse response = new MatchResponse(
                    UUID.randomUUID(), "key-1", p1, p1, p2, p3, p4,
                    "PENDING_APPROVAL", List.of(new GameDto(10, 8)), Instant.now(),
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null
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
                    List.of(new GameDto(10, 8)),
                    null, null
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
        @DisplayName("[P0] AC3: Should return PARTIALLY_CONFIRMED with context fields for 2v2 standard first confirmation")
        void shouldReturnPartiallyConfirmedWithContextFields() throws Exception {
            var matchId = UUID.randomUUID();
            var user = com.tictactore.model.User.builder().id(p3).build();
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user, null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            var response = new MatchResponse(
                    matchId, "key-partial", p1, p1, p2, p3, p4,
                    "PARTIALLY_CONFIRMED", new java.util.ArrayList<GameDto>(), Instant.now(),
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null,
                    Match.ENTRY_MODE_PARTICIPANT, Match.MATCH_FORMAT_STANDARD,
                    java.util.List.of(p3), 2, null
            );

            when(matchService.confirmMatch(eq(matchId), eq(p3), any(String.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/confirm")
                            .header("Idempotency-Key", "idem-partial")
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PARTIALLY_CONFIRMED"))
                    .andExpect(jsonPath("$.entryMode").value("PARTICIPANT"))
                    .andExpect(jsonPath("$.matchFormat").value("STANDARD"))
                    .andExpect(jsonPath("$.requiredConfirmations").value(2))
                    .andExpect(jsonPath("$.confirmedByOpponentIds").exists());
        }

        @Test
        @DisplayName("[P0] AC5: Should return CONFIRMED with referee entryMode when 2v2 referee has 1 per team")
        void shouldReturnConfirmedFor2v2RefereeWithOnePerTeam() throws Exception {
            var matchId = UUID.randomUUID();
            var user = com.tictactore.model.User.builder().id(p2).build();
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user, null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            var response = new MatchResponse(
                    matchId, "key-referee", p1, p2, p2, p3, p3,
                    "CONFIRMED", new java.util.ArrayList<GameDto>(), Instant.now(), p2, Instant.now(),
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null,
                    Match.ENTRY_MODE_REFEREE, Match.MATCH_FORMAT_STANDARD,
                    java.util.List.of(p2, p3), 2, null
            );

            when(matchService.confirmMatch(eq(matchId), eq(p2), any(String.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/confirm")
                            .header("Idempotency-Key", "idem-referee")
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.entryMode").value("REFEREE"))
                    .andExpect(jsonPath("$.requiredConfirmations").value(2));
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
        @DisplayName("[P0] AC1: Should return cooldownExpiresAt in JSON when 2v2 standard first opponent confirms")
        void shouldReturnCooldownExpiresAt_whenFirst2v2StandardConfirm() throws Exception {
            var matchId = UUID.randomUUID();
            var user = com.tictactore.model.User.builder().id(p3).build();
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user, null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            var cooldownExpiresAt = Instant.now().plusSeconds(24 * 60 * 60);
            var response = new MatchResponse(
                    matchId, "key-cooldown-api", p1, p1, p2, p3, p4,
                    "PARTIALLY_CONFIRMED", List.of(), Instant.now(),
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null,
                    Match.ENTRY_MODE_PARTICIPANT, Match.MATCH_FORMAT_STANDARD,
                    List.of(p3), 2, cooldownExpiresAt
            );

            when(matchService.confirmMatch(eq(matchId), eq(p3), any(String.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/confirm")
                            .header("Idempotency-Key", "idem-cooldown-api")
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PARTIALLY_CONFIRMED"))
                    .andExpect(jsonPath("$.cooldownExpiresAt").exists());
        }

        @Test
        @DisplayName("[P0] AC2: Should return null cooldownExpiresAt when second opponent confirms and match becomes CONFIRMED")
        void shouldReturnNullCooldown_whenSecondConfirmClearsCooldown() throws Exception {
            var matchId = UUID.randomUUID();
            var user = com.tictactore.model.User.builder().id(p4).build();
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user, null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            var response = new MatchResponse(
                    matchId, "key-cooldown-clear", p1, p1, p2, p3, p4,
                    "CONFIRMED", List.of(), Instant.now(),
                    p4, Instant.now(), null, null, null, null, null, null, null, null,
                    null, null, null, null, null,
                    Match.ENTRY_MODE_PARTICIPANT, Match.MATCH_FORMAT_STANDARD,
                    List.of(p3, p4), 2, null
            );

            when(matchService.confirmMatch(eq(matchId), eq(p4), any(String.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/confirm")
                            .header("Idempotency-Key", "idem-cooldown-clear")
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.cooldownExpiresAt").doesNotExist());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/matches/pending Specs")
    class GetPendingMatchesSpecs {

        @BeforeEach
        void setUpContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("[P0] Should return 200 OK with pending matches list")
        void shouldReturn200WithPendingMatches() throws Exception {
            var user = User.builder().id(p1).build();
            var auth = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            var response = new MatchResponse(
                    UUID.randomUUID(), "key-1", p2, p2, null, p1, null,
                    "PENDING_APPROVAL", List.of(new GameDto(10, 5)), Instant.now(),
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null
            );
            var pendingResponse = new PendingMatchesResponse(1, List.of(response));
            when(matchService.getPendingMatches(p1)).thenReturn(pendingResponse);

            mockMvc.perform(get("/api/v1/matches/pending").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(1))
                    .andExpect(jsonPath("$.matches[0].status").value("PENDING_APPROVAL"));
        }

        @Test
        @DisplayName("[P0] AC3: Should include PARTIALLY_CONFIRMED matches with context fields in pending list")
        void shouldReturnPartiallyConfirmedWithContextFieldsInPending() throws Exception {
            var user = User.builder().id(p1).build();
            var auth = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);

            var partialResponse = new MatchResponse(
                    UUID.randomUUID(), "key-partial", p1, p1, null, p2, null,
                    "PARTIALLY_CONFIRMED", new java.util.ArrayList<GameDto>(), Instant.now(),
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null,
                    Match.ENTRY_MODE_PARTICIPANT, Match.MATCH_FORMAT_STANDARD,
                    java.util.List.of(p2), 2, null
            );
            var pendingResponse = new PendingMatchesResponse(1, List.of(partialResponse));
            when(matchService.getPendingMatches(p1)).thenReturn(pendingResponse);

            mockMvc.perform(get("/api/v1/matches/pending").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(1))
                    .andExpect(jsonPath("$.matches[0].status").value("PARTIALLY_CONFIRMED"))
                    .andExpect(jsonPath("$.matches[0].entryMode").value("PARTICIPANT"))
                    .andExpect(jsonPath("$.matches[0].matchFormat").value("STANDARD"))
                     .andExpect(jsonPath("$.matches[0].requiredConfirmations").value(2));
        }

        @Test
        @DisplayName("[P0] AC1: Should include cooldownExpiresAt in pending list for PARTIALLY_CONFIRMED 2v2 standard match")
        void shouldIncludeCooldownExpiresAt_forPartiallyConfirmed2v2Standard() throws Exception {
            var user = User.builder().id(p1).build();
            var auth = new UsernamePasswordAuthenticationToken(
                    user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            var cooldownExpiresAt = Instant.now().plusSeconds(24 * 60 * 60);
            var partialResponse = new MatchResponse(
                    UUID.randomUUID(), "key-cooldown-pending", p1, p1, p2, p3, p4,
                    "PARTIALLY_CONFIRMED", new java.util.ArrayList<GameDto>(), Instant.now(),
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null,
                    Match.ENTRY_MODE_PARTICIPANT, Match.MATCH_FORMAT_STANDARD,
                    java.util.List.of(p3), 2, cooldownExpiresAt
            );
            var pendingResponse = new PendingMatchesResponse(1, List.of(partialResponse));
            when(matchService.getPendingMatches(p1)).thenReturn(pendingResponse);

            mockMvc.perform(get("/api/v1/matches/pending").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.matches[0].cooldownExpiresAt").exists());
        }

        @Test
        @DisplayName("[P1] Should not include cooldownExpiresAt for PENDING_APPROVAL matches")
        void shouldNotIncludeCooldownExpiresAt_forPendingApproval() throws Exception {
            var user = User.builder().id(p1).build();
            var auth = new UsernamePasswordAuthenticationToken(
                    user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            var pendingMatch = new MatchResponse(
                    UUID.randomUUID(), "key-pending-no-cooldown", p2, p2, null, p1, null,
                    "PENDING_APPROVAL", List.of(), Instant.now(),
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null,
                    null, null,
                    List.of(), 1, null
            );
            var pendingResponse = new PendingMatchesResponse(1, List.of(pendingMatch));
            when(matchService.getPendingMatches(p1)).thenReturn(pendingResponse);

            mockMvc.perform(get("/api/v1/matches/pending").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.matches[0].cooldownExpiresAt").doesNotExist());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/matches/{id}/reject Specs")
    class PostMatchRejectSpecs {

        @BeforeEach
        void setUpContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("[P0] Should return 200 OK when match rejection succeeds with valid reason")
        void shouldReturn200OnSuccessfulRejection() throws Exception {
            var matchId = UUID.randomUUID();
            var user = User.builder().id(p2).build();
            var auth = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            var rejectionRequest = new MatchRejectionRequest("Wrong score", "Game 1 was 10-5");
            var response = new MatchResponse(
                    matchId, "key-1", p1, p1, null, p2, null,
                    "REJECTED", List.of(), Instant.now(), null, null,
                    p2, Instant.now(), "Wrong score: Game 1 was 10-5",
                    null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null
            );
            when(matchService.rejectMatch(eq(matchId), eq(p2), any(), eq("idem-reject-1"))).thenReturn(response);

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/reject")
                            .header("Idempotency-Key", "idem-reject-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rejectionRequest))
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJECTED"))
                    .andExpect(jsonPath("$.rejectedByUserId").value(p2.toString()))
                    .andExpect(jsonPath("$.rejectionReason").value("Wrong score: Game 1 was 10-5"));
        }

        @Test
        @DisplayName("[P1] Should return 400 Bad Request when rejection reason is blank")
        void shouldReturn400OnBlankReason() throws Exception {
            var matchId = UUID.randomUUID();
            var user = User.builder().id(p2).build();
            var auth = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            var blankRequest = new MatchRejectionRequest("", null);

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(blankRequest))
                            .principal(auth))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[P1] Should return 403 Forbidden when unauthorized user attempts rejection")
        void shouldReturn403OnUnauthorizedUser() throws Exception {
            var matchId = UUID.randomUUID();
            var user = User.builder().id(p1).build();
            var auth = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            var rejectionRequest = new MatchRejectionRequest("Wrong score", null);
            when(matchService.rejectMatch(eq(matchId), eq(p1), any(), any()))
                    .thenThrow(new UnauthorizedMatchActionException("User is not an opponent"));

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rejectionRequest))
                            .principal(auth))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("User is not an opponent"));
        }

        @Test
        @DisplayName("[P1] Should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            var matchId = UUID.randomUUID();
            var rejectionRequest = new MatchRejectionRequest("Wrong score", null);
            SecurityContextHolder.clearContext();

            mockMvc.perform(post("/api/v1/matches/" + matchId + "/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rejectionRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/matches/{id} Specs")
    class DeleteMatchSpecs {

        @Test
        @DisplayName("[P0] Should return 204 No Content when match is successfully deleted by authorized user")
        void shouldReturn204OnSuccessfulDeletion() throws Exception {
            var matchId = UUID.randomUUID();
            var user = User.builder().id(p1).build();
            var auth = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);

            org.mockito.Mockito.doNothing().when(matchService).deleteMatch(matchId, p1);

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/matches/" + matchId)
                            .principal(auth))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("[P1] Should return 401 Unauthorized when unauthenticated request to delete match")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            var matchId = UUID.randomUUID();
            SecurityContextHolder.clearContext();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/matches/" + matchId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
