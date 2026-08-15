package com.tictactore.controller;

import com.tictactore.dto.LeaderboardEntry;
import com.tictactore.dto.PageResponse;
import com.tictactore.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API contract tests for {@link StatisticsController} — GET /api/v1/statistics/leaderboard.
 *
 * <p>Story 4.2: Global Leaderboard with Filtering.
 * Verifies the HTTP contract (auth, response shape, validation, parameter delegation)
 * in isolation from the aggregation logic, which is covered by the real-data
 * {@link StatisticsControllerIT} and the unit-level {@link com.tictactore.service.LeaderboardServiceTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("StatisticsController API Contract Tests")
class StatisticsControllerTest {

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

    private LeaderboardEntry entry(UUID id, String name, int total, int wins, int losses, double rate) {
        return new LeaderboardEntry(id, name, total, wins, losses, rate);
    }

    private void stub(PageResponse<LeaderboardEntry> page) {
        lenient().when(leaderboardService.getLeaderboard(any(), any(), anyInt(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);
    }

    @Nested
    @DisplayName("Authentication & Authorization")
    class AuthenticationSpecs {

        @Test
        @DisplayName("[P0] Should return 401 when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Authenticated Happy Path")
    class HappyPathSpecs {

        @Test
        @WithMockUser
        @DisplayName("[P0] Should return 200 with paginated leaderboard when authenticated")
        void shouldReturn200WithLeaderboard() throws Exception {
            stub(new PageResponse<>(List.of(entry(p1, "Alice", 10, 8, 2, 0.8), entry(p2, "Bob", 5, 2, 3, 0.4)), 1, 2, 20, 0));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
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
        @WithMockUser
        @DisplayName("[P0] Should return pagination metadata in response")
        void shouldReturnPaginationMetadata() throws Exception {
            stub(new PageResponse<>(List.of(entry(p1, "Alice", 5, 3, 2, 0.6)), 3, 25, 10, 1));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("page", "1")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPages").value(3))
                    .andExpect(jsonPath("$.totalElements").value(25))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(1));
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] Should return 200 with empty content when no players match filters")
        void shouldReturnEmptyContentWhenNoResults() throws Exception {
            stub(new PageResponse<>(List.of(), 0, 0, 20, 0));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0));
        }
    }

    @Nested
    @DisplayName("Parameter Validation")
    class ParameterValidationSpecs {

        @Test
        @WithMockUser
        @DisplayName("[P1] Should return 400 when page is negative")
        void shouldReturn400WhenPageNegative() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("page", "-1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] Should return 400 when size is zero")
        void shouldReturn400WhenSizeZero() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("size", "0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] Should return 400 when minMatches is negative")
        void shouldReturn400WhenMinMatchesNegative() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("minMatches", "-1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("[P2] Should return 400 when period is invalid")
        void shouldReturn400WhenPeriodInvalid() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("period", "INVALID")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("[P2] Should return 400 when matchFormat is invalid")
        void shouldReturn400WhenMatchFormatInvalid() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("matchFormat", "INVALID")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("[P2] Should return 400 when matchType is invalid")
        void shouldReturn400WhenMatchTypeInvalid() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("matchType", "INVALID")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Parameter Defaults & Delegation")
    class DefaultsAndDelegationSpecs {

        @Test
        @WithMockUser
        @DisplayName("[P1] Should pass default minMatches=5, page=0, size=20 to the service")
        void shouldPassDefaultParamsToService() throws Exception {
            stub(new PageResponse<>(List.of(), 0, 0, 20, 0));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .accept(MediaType.APPLICATION_JSON));

            verify(leaderboardService).getLeaderboard(isNull(), isNull(), eq(5), isNull(), isNull(), eq(0), eq(20));
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] Should forward the matchFormat filter to the service")
        void shouldForwardMatchFormatToService() throws Exception {
            stub(new PageResponse<>(List.of(entry(p1, "Alice", 5, 3, 2, 0.6)), 1, 1, 20, 0));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("matchFormat", "STANDARD")
                            .accept(MediaType.APPLICATION_JSON));

            verify(leaderboardService).getLeaderboard(isNull(), isNull(), eq(5), isNull(), eq("STANDARD"), eq(0), eq(20));
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] Should forward the period filter to the service")
        void shouldForwardPeriodToService() throws Exception {
            stub(new PageResponse<>(List.of(entry(p1, "Alice", 3, 2, 1, 0.67)), 1, 1, 20, 0));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("period", "WEEKLY")
                            .accept(MediaType.APPLICATION_JSON));

            verify(leaderboardService).getLeaderboard(isNull(), eq("WEEKLY"), eq(5), isNull(), isNull(), eq(0), eq(20));
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] Should forward the matchType filter to the service")
        void shouldForwardMatchTypeToService() throws Exception {
            stub(new PageResponse<>(List.of(entry(p1, "Alice", 5, 3, 2, 0.6)), 1, 1, 20, 0));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("matchType", "1v1")
                            .accept(MediaType.APPLICATION_JSON));

            verify(leaderboardService).getLeaderboard(isNull(), isNull(), eq(5), eq("1v1"), isNull(), eq(0), eq(20));
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] Should forward the position type filter to the service")
        void shouldForwardTypeToService() throws Exception {
            stub(new PageResponse<>(List.of(entry(p1, "Alice", 5, 3, 2, 0.6)), 1, 1, 20, 0));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("type", "ATTACKER")
                            .accept(MediaType.APPLICATION_JSON));

            verify(leaderboardService).getLeaderboard(eq("ATTACKER"), isNull(), eq(5), isNull(), isNull(), eq(0), eq(20));
        }
    }
}
