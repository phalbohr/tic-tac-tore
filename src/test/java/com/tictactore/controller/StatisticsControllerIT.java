package com.tictactore.controller;

import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.support.StatsTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Real-data API integration tests for the leaderboard endpoint.
 *
 * <p>Story 4.2: Global Leaderboard with Filtering. Seeds CONFIRMED matches into the
 * H2 test database via {@link StatsTestDataFactory} and asserts the full path
 * controller -> service -> repository -> DB, including aggregation, rule-system /
 * match-type / period filters, minMatches threshold, pagination, and security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("StatisticsController Integration Tests")
class StatisticsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.redisson.api.RedissonClient redissonClient;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.tictactore.service.TokenRevocationService tokenRevocationService;

    private UUID p1, p2, p3, p4;

    @BeforeEach
    void setUp() {
        p1 = UUID.randomUUID();
        p2 = UUID.randomUUID();
        p3 = UUID.randomUUID();
        p4 = UUID.randomUUID();
    }

    private void seedUsers(String... names) {
        UUID[] ids = new UUID[names.length];
        for (int i = 0; i < names.length; i++) {
            User user = User.builder()
                    .email(emailFor(names[i]))
                    .nickname(names[i])
                    .build();
            ids[i] = userRepository.save(user).getId();
        }
        if (names.length > 0) p1 = ids[0];
        if (names.length > 1) p2 = ids[1];
        if (names.length > 2) p3 = ids[2];
        if (names.length > 3) p4 = ids[3];
    }

    private static String emailFor(String nickname) {
        return nickname.toLowerCase().replace(" ", ".") + "@example.com";
    }

    private void seedMatches(Match... matches) {
        Stream.of(matches).forEach(matchRepository::save);
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

        @Test
        @WithMockUser
        @DisplayName("[P0] Should return 200 when authenticated (no matches yet)")
        void shouldReturn200WhenAuthenticated() throws Exception {
            seedUsers("Alice", "Bob");

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("minMatches", "1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0));
        }
    }

    @Nested
    @DisplayName("Aggregation & Sorting")
    class AggregationSpecs {

        @Test
        @WithMockUser
        @DisplayName("[P0] Should aggregate wins/losses and sort by winRate descending")
        void shouldAggregateAndSortByWinRateDesc() throws Exception {
            seedUsers("Alice", "Bob");
            // 4 CONFIRMED 1v1 matches: Alice wins 3, Bob wins 1 -> 0.75 vs 0.25
            Instant now = Instant.now();
            seedMatches(
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, now),
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, now),
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, now),
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 3, 5, now));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("minMatches", "1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].playerName").value("Alice"))
                    .andExpect(jsonPath("$.content[0].totalMatches").value(4))
                    .andExpect(jsonPath("$.content[0].wins").value(3))
                    .andExpect(jsonPath("$.content[0].losses").value(1))
                    .andExpect(jsonPath("$.content[0].winRate").value(0.75))
                    .andExpect(jsonPath("$.content[1].playerName").value("Bob"))
                    .andExpect(jsonPath("$.content[1].winRate").value(0.25))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }
    }

    @Nested
    @DisplayName("Filtering")
    class FilteringSpecs {

        @Test
        @WithMockUser
        @DisplayName("[P1] Should filter by rule system (STANDARD only)")
        void shouldFilterByMatchFormat() throws Exception {
            seedUsers("Alice", "Bob");
            Instant now = Instant.now();
            seedMatches(
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, now, Match.MATCH_FORMAT_STANDARD),
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, now, Match.MATCH_FORMAT_RANDOM));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("minMatches", "1")
                            .param("matchFormat", "STANDARD")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].totalMatches").value(1))
                    .andExpect(jsonPath("$.content[1].totalMatches").value(1))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] Should filter by match type (1v1 excludes 2v2 players)")
        void shouldFilterByMatchType() throws Exception {
            seedUsers("Alice", "Bob", "Carol", "Dave");
            Instant now = Instant.now();
            seedMatches(
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, now),
                    StatsTestDataFactory.confirmedTwoVTwo(p1, p3, p2, p4, 5, 3, now));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("minMatches", "1")
                            .param("matchType", "1v1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] Should filter by time period (WEEKLY excludes older matches)")
        void shouldFilterByPeriod() throws Exception {
            seedUsers("Alice", "Bob");
            Instant oldMatch = Instant.now().minus(30, ChronoUnit.DAYS);
            Instant recentMatch = Instant.now();
            seedMatches(
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, oldMatch),
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, recentMatch));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("minMatches", "1")
                            .param("period", "WEEKLY")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].totalMatches").value(1))
                    .andExpect(jsonPath("$.content[1].totalMatches").value(1));
        }
    }

    @Nested
    @DisplayName("Threshold & Pagination")
    class ThresholdAndPaginationSpecs {

        @Test
        @WithMockUser
        @DisplayName("[P1] Should exclude players below minMatches threshold (default 5)")
        void shouldExcludePlayersBelowThreshold() throws Exception {
            seedUsers("Alice", "Bob");
            Instant now = Instant.now();
            seedMatches(
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, now),
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, now));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0));
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] Should paginate results across multiple pages")
        void shouldPaginateResults() throws Exception {
            seedUsers("Alice", "Bob", "Carol");
            Instant now = Instant.now();
            seedMatches(
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, now),
                    StatsTestDataFactory.confirmedOneVOne(p1, p3, 5, 3, now),
                    StatsTestDataFactory.confirmedOneVOne(p2, p3, 5, 3, now));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("minMatches", "1")
                            .param("size", "1")
                            .param("page", "2")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].playerName").value("Carol"))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(3))
                    .andExpect(jsonPath("$.size").value(1))
                    .andExpect(jsonPath("$.number").value(2));
        }

        @Test
        @WithMockUser
        @DisplayName("[P2] Should return empty page content when requesting page beyond last")
        void shouldReturnEmptyWhenPageBeyondLast() throws Exception {
            seedUsers("Alice", "Bob", "Carol");
            Instant now = Instant.now();
            seedMatches(
                    StatsTestDataFactory.confirmedOneVOne(p1, p2, 5, 3, now),
                    StatsTestDataFactory.confirmedOneVOne(p1, p3, 5, 3, now),
                    StatsTestDataFactory.confirmedOneVOne(p2, p3, 5, 3, now));

            mockMvc.perform(get("/api/v1/statistics/leaderboard")
                            .param("minMatches", "1")
                            .param("size", "1")
                            .param("page", "5")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(3));
        }
    }
}
