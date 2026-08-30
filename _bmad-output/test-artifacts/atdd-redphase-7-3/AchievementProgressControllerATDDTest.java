package com.tictactore.controller;

import com.tictactore.model.Achievement;
import com.tictactore.model.PlayerAchievement;
import com.tictactore.repository.AchievementRepository;
import com.tictactore.repository.PlayerAchievementRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Story 7.3: Award Wall and Progress Tracking — REST Controller ATDD Tests (TDD Red Phase).
 *
 * AC2: Dynamic evaluation of progress for locked achievements without DB persistence
 * AC3: Non-progressive return hasProgress=false, currentProgress=null, targetValue=null;
 *      unlocked progressive return currentProgress=targetValue
 * AC4: GET /api/v1/players/{id}/achievements returns enriched DTO with progress metadata, authorization check, zero PII leak
 */
@Disabled("ATDD Red-Phase Scaffolds: Enable during Story 7.3 Task 2 & Task 3 implementation")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AchievementController Progress ATDD Tests — Story 7.3")
class AchievementProgressControllerATDDTest {

    @MockBean
    private RedissonClient redissonClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private PlayerAchievementRepository playerAchievementRepository;

    @BeforeEach
    void setUp() {
        if (achievementRepository.count() == 0) {
            achievementRepository.saveAll(List.of(
                    Achievement.builder().code("FIRST_WIN").category("MILESTONE").nameKey("achievements.first_win.title").descriptionKey("achievements.first_win.description").icon("trophy").build(),
                    Achievement.builder().code("MATCHES_10").category("EXPERIENCE").nameKey("achievements.matches_10.title").descriptionKey("achievements.matches_10.description").icon("flame").build(),
                    Achievement.builder().code("CLEAN_SHEET").category("SKILL").nameKey("achievements.clean_sheet.title").descriptionKey("achievements.clean_sheet.description").icon("shield").build(),
                    Achievement.builder().code("STRIKER_50").category("OFFENSE").nameKey("achievements.striker_50.title").descriptionKey("achievements.striker_50.description").icon("target").build(),
                    Achievement.builder().code("DEFENSE_WALL").category("DEFENSE").nameKey("achievements.defense_wall.title").descriptionKey("achievements.defense_wall.description").icon("wall").build(),
                    Achievement.builder().code("GOOSE_EGG").category("ANTI_ACHIEVEMENT").nameKey("achievements.goose_egg.title").descriptionKey("achievements.goose_egg.description").icon("egg").build()
            ));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/players/{id}/achievements Progress Metadata Specs (AC2, AC3, AC4)")
    class ProgressMetadataSpecs {

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC4] should return progress metadata fields (currentProgress, targetValue, hasProgress) in achievement DTOs")
        void shouldReturnProgressMetadataInResponse() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].hasProgress").value(true))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].targetValue").value(10))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].currentProgress").isNumber());
        }

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC3] non-progressive achievements (e.g. CLEAN_SHEET, GOOSE_EGG) should have hasProgress=false and null progress values")
        void shouldReturnNullProgressForNonProgressiveAchievements() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.achievements[?(@.code == 'CLEAN_SHEET')].hasProgress").value(false))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'CLEAN_SHEET')].currentProgress").value(nullValue()))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'CLEAN_SHEET')].targetValue").value(nullValue()))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'GOOSE_EGG')].hasProgress").value(false));
        }

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC3] unlocked progressive achievement should return currentProgress equal to targetValue")
        void shouldReturnTargetValueAsCurrentProgressWhenUnlocked() throws Exception {
            var playerId = UUID.randomUUID();
            var matchesBadge = achievementRepository.findByCode("MATCHES_10").orElseThrow();

            playerAchievementRepository.save(PlayerAchievement.builder()
                    .playerId(playerId)
                    .achievement(matchesBadge)
                    .unlockedAt(OffsetDateTime.now())
                    .build());

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].isUnlocked").value(true))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].hasProgress").value(true))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].currentProgress").value(10))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].targetValue").value(10));
        }

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC4] response should contain summary counters totalUnlocked and totalAvailable")
        void shouldReturnSummaryCounters() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalAvailable").isNumber())
                    .andExpect(jsonPath("$.totalUnlocked").isNumber());
        }

        @Test
        @DisplayName("[P0] [AC4] should return 401 Unauthorized for unauthenticated requests")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC4] response must never leak PII (email, password)")
        void shouldNotLeakPii() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.achievements[*].email").doesNotExist());
        }
    }
}
