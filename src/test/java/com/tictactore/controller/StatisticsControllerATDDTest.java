package com.tictactore.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ATDD Red-Phase Scaffolds for Statistics REST Controller (GET /api/v1/statistics/team-pairs).
 * Story 4.4: Team (Pair) Statistics
 *
 * AC 1: Pair-level performance for teammate combinations & positional synergies (attacker/defender)
 * AC 2: Filter by player, rule system, or time period
 * AC 3: Pagination and minimum matches threshold exclusion
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("StatisticsController ATDD Red Phase Scaffolds — Team Pair Statistics")
class StatisticsControllerATDDTest {

    @MockBean
    private RedissonClient redissonClient;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /api/v1/statistics/team-pairs Endpoint Specs")
    class TeamPairsEndpointSpecs {

        @Test
        @WithMockUser
        @DisplayName("[P0] GET /api/v1/statistics/team-pairs should return 200 OK with paginated team pair statistics")
        void shouldReturn200WithTeamPairStats() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/team-pairs")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.page").isNumber())
                    .andExpect(jsonPath("$.size").isNumber())
                    .andExpect(jsonPath("$.totalElements").isNumber());
        }

        @Test
        @WithMockUser
        @DisplayName("[P0] GET /api/v1/statistics/team-pairs should differentiate positional synergies (attacker vs defender)")
        void shouldDifferentiatePositionalSynergies() throws Exception {
            UUID playerA = UUID.randomUUID();
            UUID playerB = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/statistics/team-pairs")
                            .param("playerId", playerA.toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] GET /api/v1/statistics/team-pairs should filter by period, ruleConfigId, and minMatches")
        void shouldFilterByPeriodRuleAndMinMatches() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/team-pairs")
                            .param("period", "LAST_MONTH")
                            .param("ruleConfigId", UUID.randomUUID().toString())
                            .param("minMatches", "5")
                            .param("page", "0")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] GET /api/v1/statistics/team-pairs should support pagination parameters")
        void shouldSupportPagination() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/team-pairs")
                            .param("page", "1")
                            .param("size", "5")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.size").value(5));
        }
    }
}
