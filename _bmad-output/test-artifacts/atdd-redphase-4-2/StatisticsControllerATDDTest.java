package com.tictactore.controller;

import com.tictactore.dto.LeaderboardEntry;
import com.tictactore.dto.PageResponse;
import com.tictactore.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ATDD Red-Phase Scaffolds for StatisticsController GET /leaderboard.
 * Story 4.2: Global Leaderboard with Filtering
 *
 * Provider endpoint: new — not yet implemented
 *
 * Provider Scrutiny Evidence:
 * - Handler: NEW — TDD red phase
 * - Expected from acceptance criteria:
 *   - Endpoint: GET /api/v1/statistics/leaderboard
 *   - Status: 200 for success, 401 for unauthenticated, 400 for invalid params
 *   - Response: PageResponse<LeaderboardEntry> where LeaderboardEntry = { playerId: UUID, playerName: String, totalMatches: int, wins: int, losses: int, winRate: double }
 *   - Auth: authenticated (JWT required)
 *   - Filters: type (OVERALL|ATTACKER|DEFENDER), period (WEEKLY|MONTHLY|YEARLY|ALL_TIME), matchFormat (STANDARD|RANDOM), matchType (1v1|2v2), minMatches, page, size
 */
@WebMvcTest(StatisticsController.class)
@Import({com.tictactore.config.SecurityConfig.class, com.tictactore.security.JwtAuthenticationFilter.class})
@DisplayName("StatisticsController ATDD Specifications — Leaderboard")
class StatisticsControllerATDDTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaderboardService leaderboardService;

    private UUID p1, p2;

    @BeforeEach
    void setUp() {
        p1 = UUID.randomUUID();
        p2 = UUID.randomUUID();
    }

    @Nested
    @DisplayName("GET /api/v1/statistics/leaderboard Endpoint Specs")
    class LeaderboardEndpointSpecs {

        @Test
        @Disabled
        @DisplayName("[P0] Should return 200 with paginated leaderboard when authenticated and filters valid")
        void shouldReturn200WithLeaderboard() throws Exception {
            var entry1 = new LeaderboardEntry(p1, "Alice", 10, 8, 2, 0.8);
            var entry2 = new LeaderboardEntry(p2, "Bob", 5, 2, 3, 0.4);
            var page = new PageResponse<>(List.of(entry1, entry2), 1, 2, 20, 0);

            when(leaderboardService.getLeaderboard(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(page);

            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").hasSize(2))
                    .andExpect(jsonPath("$.content[0].playerId").value(p1.toString()))
                    .andExpect(jsonPath("$.content[0].playerName").value("Alice"))
                    .andExpect(jsonPath("$.content[0].totalMatches").value(10))
                    .andExpect(jsonPath("$.content[0].wins").value(8))
                    .andExpect(jsonPath("$.content[0].losses").value(2))
                    .andExpect(jsonPath("$.content[0].winRate").value(0.8))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.number").value(0));
        }

        @Test
        @Disabled
        @DisplayName("[P0] Should return 401 when request is unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Disabled
        @DisplayName("[P1] Should return 400 when page is negative")
        void shouldReturn400WhenPageNegative() throws Exception {
            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .param("page", "-1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Disabled
        @DisplayName("[P1] Should return 400 when size is zero")
        void shouldReturn400WhenSizeZero() throws Exception {
            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .param("size", "0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Disabled
        @DisplayName("[P1] Should return 200 with empty content when no players match filters")
        void shouldReturnEmptyContentWhenNoMatches() throws Exception {
            when(leaderboardService.getLeaderboard(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(new PageResponse<>(List.of(), 0, 0, 20, 0));

            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0));
        }

        @Test
        @Disabled
        @DisplayName("[P1] Should filter by matchFormat (STANDARD)")
        void shouldFilterByMatchFormat() throws Exception {
            var entry = new LeaderboardEntry(p1, "Alice", 5, 3, 2, 0.6);
            when(leaderboardService.getLeaderboard(any(), any(), any(), eq("STANDARD"), any(), any(), any()))
                    .thenReturn(new PageResponse<>(List.of(entry), 1, 1, 20, 0));

            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .param("matchFormat", "STANDARD")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").hasSize(1));
        }

        @Test
        @Disabled
        @DisplayName("[P1] Should filter by matchType (1v1)")
        void shouldFilterByMatchType() throws Exception {
            var entry = new LeaderboardEntry(p1, "Alice", 5, 3, 2, 0.6);
            when(leaderboardService.getLeaderboard(any(), any(), any(), any(), eq("1v1"), any(), any()))
                    .thenReturn(new PageResponse<>(List.of(entry), 1, 1, 20, 0));

            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .param("matchType", "1v1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").hasSize(1));
        }

        @Test
        @Disabled
        @DisplayName("[P1] Should filter by period (WEEKLY)")
        void shouldFilterByPeriod() throws Exception {
            var entry = new LeaderboardEntry(p1, "Alice", 3, 2, 1, 0.67);
            when(leaderboardService.getLeaderboard(any(), eq("WEEKLY"), any(), any(), any(), any(), any()))
                    .thenReturn(new PageResponse<>(List.of(entry), 1, 1, 20, 0));

            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .param("period", "WEEKLY")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").hasSize(1));
        }

        @Test
        @Disabled
        @DisplayName("[P1] Should respect minMatches threshold via service delegation")
        void shouldRespectMinMatchesThreshold() throws Exception {
            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .param("minMatches", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify the threshold was passed to the service layer
            // The actual filtering happens in the service; controller just delegates
        }

        @Test
        @Disabled
        @DisplayName("[P2] Should validate period enum values")
        void shouldValidatePeriodEnum() throws Exception {
            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .param("period", "INVALID")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Disabled
        @DisplayName("[P2] Should validate matchFormat enum values")
        void shouldValidateMatchFormatEnum() throws Exception {
            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .param("matchFormat", "INVALID")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Disabled
        @DisplayName("[P2] Should validate matchType enum values")
        void shouldValidateMatchTypeEnum() throws Exception {
            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .param("matchType", "INVALID")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Disabled
        @DisplayName("[P2] Should return pagination metadata in response")
        void shouldReturnPaginationMetadata() throws Exception {
            var entry = new LeaderboardEntry(p1, "Alice", 5, 3, 2, 0.6);
            when(leaderboardService.getLeaderboard(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(new PageResponse<>(List.of(entry), 1, 1, 20, 0));

            var auth = new UsernamePasswordAuthenticationToken(
                    "user@example.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .with(authentication(auth))
                            .param("page", "0")
                            .param("size", "20")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.number").value(0));
        }
    }
}
