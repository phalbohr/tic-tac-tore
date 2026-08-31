package com.tictactore.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Story 7.5: InsightController REST API ATDD Tests")
class InsightControllerATDDTest {

    @MockBean
    private RedissonClient redissonClient;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /api/v1/players/{id}/insights (AC1, AC4)")
    class PlayerInsightsEndpointSpecs {

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC4] should return 200 OK with PlayerInsightsResponse containing insights list and totalCount")
        void shouldReturnPlayerInsights_whenAuthenticated() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/insights", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.playerId").value(playerId.toString()))
                    .andExpect(jsonPath("$.totalCount").isNumber())
                    .andExpect(jsonPath("$.insights").isArray());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("[P0] [AC4] should return 401 Unauthorized when request is unauthenticated")
        void shouldReturn401_whenUnauthenticated() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/insights", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC4] response must never leak PII or email fields (AD-04, AD-05)")
        void shouldNotLeakPii_inPlayerInsightsResponse() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/insights", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.insights[*].email").doesNotExist());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/statistics/insights (AC1, AC4)")
    class CurrentUserInsightsEndpointSpecs {

        @Test
        @WithMockUser(username = "current-user@example.com")
        @DisplayName("[P0] [AC4] should return 200 OK with insights for the authenticated principal")
        void shouldReturnCurrentUserInsights_whenAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/insights")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").isNumber())
                    .andExpect(jsonPath("$.insights").isArray());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("[P0] [AC4] should return 401 Unauthorized for unauthenticated currentUser insights call")
        void shouldReturn401_whenCurrentUserUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/insights")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
