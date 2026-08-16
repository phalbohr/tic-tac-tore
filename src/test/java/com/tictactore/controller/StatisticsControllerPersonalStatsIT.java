package com.tictactore.controller;

import com.tictactore.dto.PlayerStatsResponse;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import static org.hamcrest.Matchers.closeTo;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("[Story 4.3] StatisticsController /me Endpoint — RED Phase")
class StatisticsControllerPersonalStatsIT {

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

    private UUID aliceId, bobId, carolId, daveId;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    private UUID seedUser(String nickname) {
        User user = User.builder()
                .email(nickname.toLowerCase().replace(" ", ".") + "@example.com")
                .nickname(nickname)
                .build();
        return userRepository.save(user).getId();
    }

    private void seedMatches(Match... matches) {
        for (Match match : matches) {
            matchRepository.save(match);
        }
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(UUID userId) {
        User managedUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return new UsernamePasswordAuthenticationToken(managedUser, null, List.of());
    }

    @Nested
    @DisplayName("Authentication & Authorization")
    class AuthenticationSpecs {

        @Test
        @DisplayName("[P0] Should return 401 when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/statistics/me")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("[P0] Should return 200 with PlayerStatsResponse shape when authenticated with app User principal")
        void shouldReturn200WithPlayerStatsResponseWhenAuthenticated() throws Exception {
            aliceId = seedUser("Alice");

            mockMvc.perform(get("/api/v1/statistics/me")
                            .with(authentication(buildAuthentication(aliceId)))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.playerId").value(aliceId.toString()))
                    .andExpect(jsonPath("$.playerName").value("Alice"))
                    .andExpect(jsonPath("$.overall.matches").value(0))
                    .andExpect(jsonPath("$.overall.wins").value(0))
                    .andExpect(jsonPath("$.overall.losses").value(0))
                    .andExpect(jsonPath("$.overall.winRate").value(0.0))
                    .andExpect(jsonPath("$.attacker.matches").value(0))
                    .andExpect(jsonPath("$.attacker.wins").value(0))
                    .andExpect(jsonPath("$.attacker.losses").value(0))
                    .andExpect(jsonPath("$.attacker.winRate").value(0.0))
                    .andExpect(jsonPath("$.defender.matches").value(0))
                    .andExpect(jsonPath("$.defender.wins").value(0))
                    .andExpect(jsonPath("$.defender.losses").value(0))
                    .andExpect(jsonPath("$.defender.winRate").value(0.0));
        }
    }

    @Nested
    @DisplayName("Aggregation & Positional Breakdown")
    class AggregationSpecs {

        @Test
        @DisplayName("[P0] Should compute correct per-position stats from CONFIRMED 1v1 matches")
        void shouldAggregatePerPositionStatsFromConfirmedMatches() throws Exception {
            aliceId = seedUser("Alice");
            bobId = seedUser("Bob");
            Instant now = Instant.now();
            seedMatches(
                    StatsTestDataFactory.confirmedOneVOne(aliceId, bobId, 10, 8, now),
                    StatsTestDataFactory.confirmedOneVOne(aliceId, bobId, 5, 10, now),
                    StatsTestDataFactory.confirmedOneVOne(bobId, aliceId, 10, 5, now)
            );

            mockMvc.perform(get("/api/v1/statistics/me")
                            .with(authentication(buildAuthentication(aliceId)))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.playerId").value(aliceId.toString()))
                    .andExpect(jsonPath("$.playerName").value("Alice"))
                    .andExpect(jsonPath("$.overall.matches").value(3))
                    .andExpect(jsonPath("$.overall.wins").value(1))
                    .andExpect(jsonPath("$.overall.losses").value(2))
                    .andExpect(jsonPath("$.overall.winRate", closeTo(33.3, 0.1)))
                    .andExpect(jsonPath("$.attacker.matches").value(3))
                    .andExpect(jsonPath("$.attacker.wins").value(1))
                    .andExpect(jsonPath("$.attacker.losses").value(2))
                    .andExpect(jsonPath("$.defender.matches").value(0))
                    .andExpect(jsonPath("$.defender.wins").value(0))
                    .andExpect(jsonPath("$.defender.losses").value(0));
        }

        @Test
        @DisplayName("[P0] 0-match user -> all positions empty with winRate 0.0")
        void shouldReturnEmptyStatsForUserWithNoMatches() throws Exception {
            aliceId = seedUser("Alice");

            mockMvc.perform(get("/api/v1/statistics/me")
                            .with(authentication(buildAuthentication(aliceId)))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.playerId").value(aliceId.toString()))
                    .andExpect(jsonPath("$.playerName").value("Alice"))
                    .andExpect(jsonPath("$.overall.matches").value(0))
                    .andExpect(jsonPath("$.overall.wins").value(0))
                    .andExpect(jsonPath("$.overall.losses").value(0))
                    .andExpect(jsonPath("$.overall.winRate").value(0.0))
                    .andExpect(jsonPath("$.attacker.matches").value(0))
                    .andExpect(jsonPath("$.defender.matches").value(0));
        }

        @Test
        @DisplayName("[P0] Fully-tied match counts as totalMatches only, no win/loss increment")
        void shouldCountTiedMatchAsTotalMatchesOnly() throws Exception {
            aliceId = seedUser("Alice");
            bobId = seedUser("Bob");
            Instant now = Instant.now();
            seedMatches(
                    StatsTestDataFactory.confirmedOneVOne(aliceId, bobId, 10, 10, now)
            );

            mockMvc.perform(get("/api/v1/statistics/me")
                            .with(authentication(buildAuthentication(aliceId)))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.overall.matches").value(1))
                    .andExpect(jsonPath("$.overall.wins").value(0))
                    .andExpect(jsonPath("$.overall.losses").value(0))
                    .andExpect(jsonPath("$.overall.winRate").value(0.0))
                    .andExpect(jsonPath("$.attacker.matches").value(1))
                    .andExpect(jsonPath("$.attacker.wins").value(0))
                    .andExpect(jsonPath("$.attacker.losses").value(0));
        }

        @Test
        @DisplayName("[P1] 2v2 match: user as attacker gets attacker stats; as defender gets defender stats")
        void shouldTrackAttackerAndDefenderStatsInTwoVTwo() throws Exception {
            aliceId = seedUser("Alice");
            bobId = seedUser("Bob");
            carolId = seedUser("Carol");
            daveId = seedUser("Dave");
            Instant now = Instant.now();
            seedMatches(
                    StatsTestDataFactory.confirmedTwoVTwo(aliceId, carolId, bobId, daveId, 10, 8, now)
            );

            mockMvc.perform(get("/api/v1/statistics/me")
                            .with(authentication(buildAuthentication(aliceId)))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.overall.matches").value(1))
                    .andExpect(jsonPath("$.attacker.matches").value(1))
                    .andExpect(jsonPath("$.attacker.wins").value(1))
                    .andExpect(jsonPath("$.attacker.losses").value(0))
                    .andExpect(jsonPath("$.defender.matches").value(0));

            mockMvc.perform(get("/api/v1/statistics/me")
                            .with(authentication(buildAuthentication(carolId)))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.overall.matches").value(1))
                    .andExpect(jsonPath("$.attacker.matches").value(0))
                    .andExpect(jsonPath("$.defender.matches").value(1))
                    .andExpect(jsonPath("$.defender.wins").value(1))
                    .andExpect(jsonPath("$.defender.losses").value(0));
        }
    }

    @Nested
    @DisplayName("Confirmation Status Filtering")
    class ConfirmationFilteringSpecs {

        @Test
        @DisplayName("[P0] Excludes PENDING matches; only CONFIRMED matches counted")
        void shouldExcludePendingMatches() throws Exception {
            aliceId = seedUser("Alice");
            bobId = seedUser("Bob");
            Instant now = Instant.now();
            seedMatches(
                    StatsTestDataFactory.confirmedOneVOne(aliceId, bobId, 10, 8, now),
                    StatsTestDataFactory.pendingOneVOne(aliceId, bobId, 10, 8, now)
            );

            mockMvc.perform(get("/api/v1/statistics/me")
                            .with(authentication(buildAuthentication(aliceId)))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.overall.matches").value(1))
                    .andExpect(jsonPath("$.overall.wins").value(1))
                    .andExpect(jsonPath("$.overall.losses").value(0));
        }
    }
}
