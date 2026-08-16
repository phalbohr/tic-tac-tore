package com.tictactore.service;

import com.tictactore.dto.LeaderboardEntry;
import com.tictactore.dto.PageResponse;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.LeaderboardRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.impl.LeaderboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LeaderboardService Unit Tests")
class LeaderboardServiceTest {

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    private UUID p1, p2, p3, p4;

    @BeforeEach
    void setUp() {
        p1 = UUID.randomUUID();
        p2 = UUID.randomUUID();
        p3 = UUID.randomUUID();
        p4 = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Aggregation Tests")
    class AggregationTests {

        @Test
        @DisplayName("[P0] Should compute correct win/loss stats from CONFIRMED matches")
        void shouldAggregateStatsCorrectly() {
            Match match1 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(
                            new Game(null, null, 1, 10, 8, null, null, null, null, null),
                            new Game(null, null, 2, 10, 6, null, null, null, null, null)
                    ))
                    .build();

            Match match2 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(
                            new Game(null, null, 1, 5, 10, null, null, null, null, null),
                            new Game(null, null, 2, 5, 10, null, null, null, null, null)
                    ))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(match1, match2));
            when(userRepository.findById(any())).thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
            });
            when(userRepository.findAllById(any())).thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                List<User> users = new ArrayList<>();
                for (UUID id : ids) {
                    users.add(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
                }
                return users;
            });

            PageResponse<LeaderboardEntry> response = leaderboardService.getLeaderboard(
                    "OVERALL", "ALL_TIME", 1, null, null, 0, 10
            );

            assertThat(response.content()).hasSize(2);
            assertThat(response.totalElements()).isEqualTo(2);

            LeaderboardEntry p1Entry = response.content().stream()
                    .filter(e -> e.playerId().equals(p1)).findFirst().orElseThrow();
            assertThat(p1Entry.totalMatches()).isEqualTo(2);
            assertThat(p1Entry.wins()).isEqualTo(1);
            assertThat(p1Entry.losses()).isEqualTo(1);
            assertThat(p1Entry.winRate()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("[P1] Should exclude players below minMatches threshold")
        void shouldExcludePlayersBelowThreshold() {
            Match match1 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 10, 8, null, null, null, null, null)))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(match1));
            when(userRepository.findById(any())).thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
            });
            when(userRepository.findAllById(any())).thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                List<User> users = new ArrayList<>();
                for (UUID id : ids) {
                    users.add(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
                }
                return users;
            });

            PageResponse<LeaderboardEntry> response = leaderboardService.getLeaderboard(
                    "OVERALL", "ALL_TIME", 5, null, null, 0, 10
            );

            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("[P1] Should filter by match type (1v1 vs 2v2)")
        void shouldFilterByMatchType() {
            Match match1v1 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 10, 8, null, null, null, null, null)))
                    .build();

            Match match2v2 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(p3)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(p4)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 10, 8, p3, null, p2, p4, null)))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(match1v1, match2v2));
            when(userRepository.findById(any())).thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
            });
            when(userRepository.findAllById(any())).thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                List<User> users = new ArrayList<>();
                for (UUID id : ids) {
                    users.add(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
                }
                return users;
            });

            PageResponse<LeaderboardEntry> response = leaderboardService.getLeaderboard(
                    "OVERALL", "ALL_TIME", 1, "1v1", null, 0, 10
            );

            assertThat(response.content()).hasSize(2);
            for (LeaderboardEntry entry : response.content()) {
                assertThat(entry.totalMatches()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("[P1] Should filter by rule system (match format)")
        void shouldFilterByRuleSystem() {
            Match standardMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .matchFormat("STANDARD")
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 10, 8, null, null, null, null, null)))
                    .build();

            Match randomMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .matchFormat("RANDOM")
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 10, 8, null, null, null, null, null)))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(standardMatch, randomMatch));
            when(userRepository.findById(any())).thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
            });
            when(userRepository.findAllById(any())).thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                List<User> users = new ArrayList<>();
                for (UUID id : ids) {
                    users.add(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
                }
                return users;
            });

            PageResponse<LeaderboardEntry> response = leaderboardService.getLeaderboard(
                    "OVERALL", "ALL_TIME", 1, null, "STANDARD", 0, 10
            );

            assertThat(response.content()).hasSize(2);
            for (LeaderboardEntry entry : response.content()) {
                assertThat(entry.totalMatches()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("[P1] Should filter by time period")
        void shouldFilterByTimePeriod() {
            Match oldMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS))
                    .games(List.of(new Game(null, null, 1, 10, 8, null, null, null, null, null)))
                    .build();

            Match recentMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 10, 8, null, null, null, null, null)))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(oldMatch, recentMatch));
            when(userRepository.findById(any())).thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
            });
            when(userRepository.findAllById(any())).thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                List<User> users = new ArrayList<>();
                for (UUID id : ids) {
                    users.add(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
                }
                return users;
            });

            PageResponse<LeaderboardEntry> response = leaderboardService.getLeaderboard(
                    "OVERALL", "WEEKLY", 1, null, null, 0, 10
            );

            assertThat(response.content()).hasSize(2);
            for (LeaderboardEntry entry : response.content()) {
                assertThat(entry.totalMatches()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("[P1] Should sort by winRate descending")
        void shouldSortByWinRateDescending() {
            Match match1 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 10, 8, null, null, null, null, null)))
                    .build();

            Match match2 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p2)
                    .teamADefenderId(null)
                    .teamBAttackerId(p1)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 5, 10, null, null, null, null, null)))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(match1, match2));
            when(userRepository.findById(any())).thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
            });
            when(userRepository.findAllById(any())).thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                List<User> users = new ArrayList<>();
                for (UUID id : ids) {
                    users.add(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
                }
                return users;
            });

            PageResponse<LeaderboardEntry> response = leaderboardService.getLeaderboard(
                    "OVERALL", "ALL_TIME", 1, null, null, 0, 10
            );

            assertThat(response.content()).hasSize(2);
            assertThat(response.content().get(0).winRate()).isEqualTo(1.0);
            assertThat(response.content().get(1).winRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("[P1] Should paginate results correctly")
        void shouldPaginateResults() {
            List<Match> matches = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                UUID attacker = UUID.randomUUID();
                UUID defender = UUID.randomUUID();
                matches.add(Match.builder()
                        .id(UUID.randomUUID())
                        .teamAAttackerId(attacker)
                        .teamADefenderId(null)
                        .teamBAttackerId(defender)
                        .teamBDefenderId(null)
                        .status(Match.STATUS_CONFIRMED)
                        .createdAt(Instant.now())
                        .games(List.of(new Game(null, null, 1, 10, 8, null, null, null, null, null)))
                        .build());
            }

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(matches);
            when(userRepository.findById(any())).thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
            });
            when(userRepository.findAllById(any())).thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                List<User> users = new ArrayList<>();
                for (UUID id : ids) {
                    users.add(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
                }
                return users;
            });

            PageResponse<LeaderboardEntry> page1 = leaderboardService.getLeaderboard(
                    "OVERALL", "ALL_TIME", 1, null, null, 0, 10
            );

            assertThat(page1.content()).hasSize(10);
            assertThat(page1.totalElements()).isEqualTo(50);
            assertThat(page1.totalPages()).isEqualTo(5);
            assertThat(page1.number()).isEqualTo(0);
            assertThat(page1.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("[P1] Should filter by attacker position")
        void shouldFilterByAttackerPosition() {
            Match match1v1 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 10, 8, null, null, null, null, null)))
                    .build();

            Match match2v2 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(p3)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(p4)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 10, 8, p3, null, p2, p4, null)))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(match1v1, match2v2));
            when(userRepository.findById(any())).thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
            });
            when(userRepository.findAllById(any())).thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                List<User> users = new ArrayList<>();
                for (UUID id : ids) {
                    users.add(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
                }
                return users;
            });

            PageResponse<LeaderboardEntry> response = leaderboardService.getLeaderboard(
                    "ATTACKER", "ALL_TIME", 1, null, null, 0, 10
            );

            assertThat(response.content()).hasSize(2);
            assertThat(response.content().get(0).totalMatches()).isEqualTo(2);
        }

        @Test
        @DisplayName("[P1] Should filter by defender position")
        void shouldFilterByDefenderPosition() {
            Match match2v2 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(p3)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(p4)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 10, 8, p3, null, p2, p4, null)))
                    .build();

            Match match1v1 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 10, 8, null, null, null, null, null)))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(match2v2, match1v1));
            when(userRepository.findById(any())).thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
            });
            when(userRepository.findAllById(any())).thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                List<User> users = new ArrayList<>();
                for (UUID id : ids) {
                    users.add(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
                }
                return users;
            });

            PageResponse<LeaderboardEntry> response = leaderboardService.getLeaderboard(
                    "DEFENDER", "ALL_TIME", 1, null, null, 0, 10
            );

            assertThat(response.content()).hasSize(2);
            assertThat(response.content().get(0).totalMatches()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("[P1] Should return empty content when no matches match filters")
        void shouldReturnEmptyWhenNoMatches() {
            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of());

            PageResponse<LeaderboardEntry> response = leaderboardService.getLeaderboard(
                    "OVERALL", "ALL_TIME", 1, null, null, 0, 10
            );

            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(0);
            assertThat(response.totalPages()).isEqualTo(0);
        }

        @Test
        @DisplayName("[P1] Should handle game ties correctly")
        void shouldHandleGameTies() {
            Match match = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(
                            new Game(null, null, 1, 10, 10, null, null, null, null, null),
                            new Game(null, null, 2, 10, 8, null, null, null, null, null)
                    ))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(match));
            when(userRepository.findById(any())).thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
            });
            when(userRepository.findAllById(any())).thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                List<User> users = new ArrayList<>();
                for (UUID id : ids) {
                    users.add(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
                }
                return users;
            });

            PageResponse<LeaderboardEntry> response = leaderboardService.getLeaderboard(
                    "OVERALL", "ALL_TIME", 1, null, null, 0, 10
            );

            assertThat(response.content()).hasSize(2);
            assertThat(response.content().get(0).totalMatches()).isEqualTo(1);
            assertThat(response.content().get(0).wins()).isEqualTo(1);
            assertThat(response.content().get(1).wins()).isEqualTo(0);
        }

        @Test
        @DisplayName("[P1] Should count fully tied matches as totalMatches without win/loss")
        void shouldCountFullyTiedMatches() {
            Match match = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(
                            new Game(null, null, 1, 10, 10, null, null, null, null, null)
                    ))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(match));
            when(userRepository.findById(any())).thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return Optional.of(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
            });
            when(userRepository.findAllById(any())).thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                List<User> users = new ArrayList<>();
                for (UUID id : ids) {
                    users.add(User.builder().id(id).nickname("Player-" + id.toString().substring(0, 4)).build());
                }
                return users;
            });

            PageResponse<LeaderboardEntry> response = leaderboardService.getLeaderboard(
                    "OVERALL", "ALL_TIME", 1, null, null, 0, 10
            );

            assertThat(response.content()).hasSize(2);
            for (LeaderboardEntry entry : response.content()) {
                assertThat(entry.totalMatches()).isEqualTo(1);
                assertThat(entry.wins()).isEqualTo(0);
                assertThat(entry.losses()).isEqualTo(0);
            }
        }
    }
}
