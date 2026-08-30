package com.tictactore.controller;

import com.tictactore.model.Achievement;
import com.tictactore.model.PlayerAchievement;
import com.tictactore.repository.AchievementRepository;
import com.tictactore.repository.PlayerAchievementRepository;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AchievementController Progress ATDD Tests — Story 7.3")
class AchievementProgressControllerATDDTest {

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    private com.tictactore.service.TokenRevocationService tokenRevocationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private PlayerAchievementRepository playerAchievementRepository;

    @Autowired
    private com.tictactore.repository.UserRepository userRepository;

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
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].hasProgress").value(hasItem(true)))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].targetValue").value(hasItem(10)))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].currentProgress").value(hasItem(isA(Number.class))));
        }

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC3] non-progressive achievements (e.g. CLEAN_SHEET, GOOSE_EGG) should have hasProgress=false and null progress values")
        void shouldReturnNullProgressForNonProgressiveAchievements() throws Exception {
            var playerId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.achievements[?(@.code == 'CLEAN_SHEET')].hasProgress").value(hasItem(false)))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'CLEAN_SHEET')].currentProgress").value(hasItem(nullValue())))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'CLEAN_SHEET')].targetValue").value(hasItem(nullValue())))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'GOOSE_EGG')].hasProgress").value(hasItem(false)));
        }

        @Test
        @WithMockUser
        @DisplayName("[P0] [AC3] unlocked progressive achievement should return currentProgress equal to targetValue")
        void shouldReturnTargetValueAsCurrentProgressWhenUnlocked() throws Exception {
            var user = userRepository.save(com.tictactore.model.User.builder()
                    .email("prog_user_" + UUID.randomUUID() + "@example.com")
                    .nickname("ProgUser")
                    .build());
            var playerId = user.getId();
            var matchesBadge = achievementRepository.findByCode("MATCHES_10").orElseThrow();

            playerAchievementRepository.save(PlayerAchievement.builder()
                    .user(user)
                    .achievement(matchesBadge)
                    .unlockedAt(Instant.now())
                    .build());

            mockMvc.perform(get("/api/v1/players/{id}/achievements", playerId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].isUnlocked").value(hasItem(true)))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].hasProgress").value(hasItem(true)))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].currentProgress").value(hasItem(10)))
                    .andExpect(jsonPath("$.achievements[?(@.code == 'MATCHES_10')].targetValue").value(hasItem(10)));
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
