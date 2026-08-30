package com.tictactore.controller;

import com.tictactore.model.Achievement;
import com.tictactore.repository.AchievementRepository;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ATDD Red-Phase Scaffolds for Achievement REST Controller (GET /api/v1/players/{id}/achievements).
 * Story 7.1: Achievement System (Badges)
 *
 * AC 1: Asynchronous evaluation on match confirmation
 * AC 2: Idempotent awarding of initial 5 achievements (FIRST_WIN, MATCHES_10, CLEAN_SHEET, STRIKER_50, DEFENSE_WALL)
 * AC 3: Query achievements summary without PII exposure (AD-04)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AchievementController ATDD Tests — Story 7.1")
class AchievementControllerATDDTest {

    @MockBean
    private RedissonClient redissonClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AchievementRepository achievementRepository;

    @BeforeEach
    void setUp() {
        if (achievementRepository.count() == 0) {
            achievementRepository.saveAll(List.of(
                    Achievement.builder().code("FIRST_WIN").category("MILESTONE").nameKey("achievements.first_win.title").descriptionKey("achievements.first_win.description").icon("trophy").build(),
                    Achievement.builder().code("MATCHES_10").category("EXPERIENCE").nameKey("achievements.matches_10.title").descriptionKey("achievements.matches_10.description").icon("flame").build(),
                    Achievement.builder().code("CLEAN_SHEET").category("SKILL").nameKey("achievements.clean_sheet.title").descriptionKey("achievements.clean_sheet.description").icon("shield").build(),
                    Achievement.builder().code("STRIKER_50").category("OFFENSE").nameKey("achievements.striker_50.title").descriptionKey("achievements.striker_50.description").icon("target").build(),
                    Achievement.builder().code("DEFENSE_WALL").category("DEFENSE").nameKey("achievements.defense_wall.title").descriptionKey("achievements.defense_wall.description").icon("wall").build()
            ));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/players/{id}/achievements Endpoint Specs")
    class GetPlayerAchievementsSpecs {

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC3] should return 200 OK with achievement summary for valid player ID")
        void shouldReturn200WithAchievementsSummary() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.playerId").value(playerId.toString()))
                    .andExpect(jsonPath("$.totalUnlocked").isNumber())
                    .andExpect(jsonPath("$.totalAvailable").isNumber())
                    .andExpect(jsonPath("$.achievements").isArray());
        }

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC2, AC3] should return initial 5 catalog badges with unlock status metadata")
        void shouldReturnAchievementCatalogMetadata() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.achievements[*].code", containsInAnyOrder(
                            "FIRST_WIN", "MATCHES_10", "CLEAN_SHEET", "STRIKER_50", "DEFENSE_WALL"
                    )))
                    .andExpect(jsonPath("$.achievements[0].id").isNotEmpty())
                    .andExpect(jsonPath("$.achievements[0].category").isNotEmpty())
                    .andExpect(jsonPath("$.achievements[0].nameKey").isNotEmpty())
                    .andExpect(jsonPath("$.achievements[0].descriptionKey").isNotEmpty())
                    .andExpect(jsonPath("$.achievements[0].icon").isNotEmpty())
                    .andExpect(jsonPath("$.achievements[0].isUnlocked").isBoolean());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("[P0] [AC3] should return 401 Unauthorized when request lacks authentication")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC3] [AD-04] response must never leak PII (email, password, credentials)")
        void shouldNotExposePiiInResponse() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.user.email").doesNotExist())
                    .andExpect(jsonPath("$.achievements[*].email").doesNotExist())
                    .andExpect(jsonPath("$.achievements[*].password").doesNotExist());
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] [AC3] should return 400 Bad Request when player ID format is invalid")
        void shouldReturn400WhenInvalidPlayerId() throws Exception {
            mockMvc.perform(get("/api/v1/players/{id}/achievements", "not-a-valid-uuid")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
