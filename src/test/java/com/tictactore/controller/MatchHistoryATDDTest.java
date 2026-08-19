package com.tictactore.controller;

import com.tictactore.dto.GameDto;
import com.tictactore.dto.MatchResponse;
import com.tictactore.dto.PagedResponse;
import com.tictactore.exception.GlobalExceptionHandler;
import com.tictactore.model.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ATDD Red-Phase Scaffolds for Match History REST Controller (GET /api/v1/matches/history).
 * Story 4.6: Unified Match History (My Matches)
 *
 * AC 1: Paginated chronological match history (Confirmed / Pending) via PagedResponse<MatchResponse>
 * AC 2: Filter by player (opponent/partner), match type (1v1/2v2), and rule template (ruleConfigId)
 * AC 3: Clubhouse No-Line rule compliance and retired player name resolution (AD-04)
 * Security: Authenticated user via @AuthenticationPrincipal (AD-05)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchHistory Controller ATDD Specifications")
class MatchHistoryATDDTest {

    private MockMvc mockMvc;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchController matchController;

    private UUID currentUserId;
    private User currentUser;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(matchController)
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        currentUserId = UUID.randomUUID();
        currentUser = User.builder().id(currentUserId).email("player@example.com").build();
        auth = new UsernamePasswordAuthenticationToken(currentUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("GET /api/v1/matches/history Endpoint Specs")
    class GetMatchHistorySpecs {

        @Test
        @DisplayName("[P0] Should return 200 OK with PagedResponse<MatchResponse> for authenticated user")
        void shouldReturn200WithPagedMatchHistory() throws Exception {
            UUID matchId = UUID.randomUUID();
            UUID opponentId = UUID.randomUUID();

            MatchResponse match = new MatchResponse(
                    matchId, "key-1", currentUserId, currentUserId, null, opponentId, null,
                    "CONFIRMED", List.of(new GameDto(10, 8)), Instant.now(),
                    null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null
            );

            PagedResponse<MatchResponse> pagedResponse = new PagedResponse<>(
                    List.of(match), 0, 10, 1L, 1
            );

            when(matchService.getMatchHistory(eq(currentUserId), eq("CONFIRMED"), any(), any(), any(), eq(0), eq(10)))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/matches/history")
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(matchId.toString()))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("[P0] Should enforce AD-02 status filtering (CONFIRMED vs PENDING)")
        void shouldEnforceStatusFilter() throws Exception {
            PagedResponse<MatchResponse> emptyPending = new PagedResponse<>(
                    List.of(), 0, 10, 0L, 0
            );

            when(matchService.getMatchHistory(eq(currentUserId), eq("PENDING"), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(emptyPending);

            mockMvc.perform(get("/api/v1/matches/history")
                            .param("status", "PENDING")
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("[P1] Should support filtering by playerId, matchType, and ruleConfigId")
        void shouldSupportFilters() throws Exception {
            UUID filterPlayerId = UUID.randomUUID();
            UUID ruleConfigId = UUID.randomUUID();

            PagedResponse<MatchResponse> filteredResponse = new PagedResponse<>(
                    List.of(), 0, 10, 0L, 0
            );

            when(matchService.getMatchHistory(eq(currentUserId), eq("CONFIRMED"), eq(filterPlayerId), eq(ruleConfigId), eq("1v1"), eq(0), eq(10)))
                    .thenReturn(filteredResponse);

            mockMvc.perform(get("/api/v1/matches/history")
                            .param("playerId", filterPlayerId.toString())
                            .param("ruleConfigId", ruleConfigId.toString())
                            .param("matchType", "1v1")
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("[P1] Should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(get("/api/v1/matches/history")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
