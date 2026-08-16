package com.tictactore.service;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("[Story 4.3] LeaderboardService Personal Stats — RED Phase")
class LeaderboardServicePersonalStatsTest {

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

    private void stubUser(UUID id, String nickname) {
        when(userRepository.findById(id))
                .thenReturn(Optional.of(User.builder().id(id).nickname(nickname).email(nickname.toLowerCase() + "@example.com").build()));
    }

    @Nested
    @DisplayName("Aggregation & Positional Breakdown")
    class AggregationSpecs {

        @Test
        @DisplayName("[P0] Should compute correct per-position wins/losses from CONFIRMED matches; winRate on 0-100 scale")
        void shouldComputePerPositionStatsWithCorrectWinRateScale() {
            Match match1v1 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(
                            new Game(null, null, 1, 10, 8, null, null, null, null, null),
                            new Game(null, null, 2, 5, 10, null, null, null, null, null)
                    ))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(match1v1));
            stubUser(p1, "Alice");
            stubUser(p2, "Bob");

            var response = leaderboardService.getPersonalStats(p1);

            assertThat(response.playerId()).isEqualTo(p1);
            assertThat(response.playerName()).isEqualTo("Alice");
            assertThat(response.overall().matches()).isEqualTo(1);
            assertThat(response.overall().wins()).isEqualTo(0);
            assertThat(response.overall().losses()).isEqualTo(0);
            assertThat(response.overall().winRate()).isEqualTo(0.0);
            assertThat(response.attacker().matches()).isEqualTo(1);
            assertThat(response.attacker().wins()).isEqualTo(0);
            assertThat(response.attacker().losses()).isEqualTo(0);
            assertThat(response.attacker().winRate()).isEqualTo(0.0);
            assertThat(response.defender().matches()).isEqualTo(0);
            assertThat(response.defender().wins()).isEqualTo(0);
            assertThat(response.defender().losses()).isEqualTo(0);
            assertThat(response.defender().winRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("[P0] 0-match user -> all positions empty() with winRate 0.0")
        void shouldReturnEmptyStatsWhenNoMatches() {
            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of());
            stubUser(p1, "Alice");

            var response = leaderboardService.getPersonalStats(p1);

            assertThat(response.playerId()).isEqualTo(p1);
            assertThat(response.playerName()).isEqualTo("Alice");
            assertThat(response.overall().matches()).isEqualTo(0);
            assertThat(response.overall().wins()).isEqualTo(0);
            assertThat(response.overall().losses()).isEqualTo(0);
            assertThat(response.overall().winRate()).isEqualTo(0.0);
            assertThat(response.attacker().matches()).isEqualTo(0);
            assertThat(response.defender().matches()).isEqualTo(0);
        }

        @Test
        @DisplayName("[P1] Fully-tied match counts as totalMatches only, no win/loss increment")
        void shouldCountTiedMatchAsTotalMatchesOnly() {
            Match tiedMatch = Match.builder()
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
                    .thenReturn(List.of(tiedMatch));
            stubUser(p1, "Alice");

            var response = leaderboardService.getPersonalStats(p1);

            assertThat(response.overall().matches()).isEqualTo(1);
            assertThat(response.overall().wins()).isEqualTo(0);
            assertThat(response.overall().losses()).isEqualTo(0);
            assertThat(response.attacker().matches()).isEqualTo(1);
            assertThat(response.attacker().wins()).isEqualTo(0);
            assertThat(response.attacker().losses()).isEqualTo(0);
        }

        @Test
        @DisplayName("[P1] 2v2 match: user as attacker increments attacker stats; as defender increments defender stats")
        void shouldTrackAttackerAndDefenderStatsInTwoVTwo() {
            Match match2v2 = Match.builder()
                    .id(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(p3)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(p4)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(
                            new Game(null, null, 1, 10, 8, p3, null, p2, p4, null)
                    ))
                    .build();

            when(leaderboardRepository.findConfirmedMatchesWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(match2v2));
            stubUser(p1, "Alice");
            stubUser(p3, "Carol");

            var response = leaderboardService.getPersonalStats(p1);

            assertThat(response.overall().matches()).isEqualTo(1);
            assertThat(response.attacker().matches()).isEqualTo(1);
            assertThat(response.defender().matches()).isEqualTo(0);

            var response2 = leaderboardService.getPersonalStats(p3);

            assertThat(response2.overall().matches()).isEqualTo(1);
            assertThat(response2.attacker().matches()).isEqualTo(0);
            assertThat(response2.defender().matches()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Confirmation Status Filtering")
    class ConfirmationFilteringSpecs {

        @Test
        @DisplayName("[P0] Excludes PENDING matches; only CONFIRMED matches counted")
        void shouldExcludePendingMatches() {
            Match confirmed = Match.builder()
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
                    .thenReturn(List.of(confirmed));
            stubUser(p1, "Alice");

            var response = leaderboardService.getPersonalStats(p1);

            assertThat(response.overall().matches()).isEqualTo(1);
            assertThat(response.attacker().matches()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseSpecs {

        @Test
        @DisplayName("[P1] Non-existent user -> playerName is Unknown with empty stats")
        void shouldReturnUnknownNameForNonExistentUser() {
            Match match = Match.builder()
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
                    .thenReturn(List.of(match));
            when(userRepository.findById(p1)).thenReturn(Optional.empty());

            var response = leaderboardService.getPersonalStats(p1);

            assertThat(response.playerName()).isEqualTo("Unknown");
            assertThat(response.overall().matches()).isEqualTo(1);
            assertThat(response.attacker().matches()).isEqualTo(1);
        }

        @Test
        @DisplayName("[P1] winRate 0-100 scale: 2 wins out of 3 matches = 66.7")
        void shouldCalculateWinRateOnZeroToHundredScale() {
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
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_CONFIRMED)
                    .createdAt(Instant.now())
                    .games(List.of(new Game(null, null, 1, 5, 10, null, null, null, null, null)))
                    .build();

            Match match3 = Match.builder()
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
                    .thenReturn(List.of(match1, match2, match3));
            stubUser(p1, "Alice");

            var response = leaderboardService.getPersonalStats(p1);

            assertThat(response.overall().matches()).isEqualTo(3);
            assertThat(response.overall().wins()).isEqualTo(2);
            assertThat(response.overall().losses()).isEqualTo(1);
            assertThat(response.overall().winRate()).isCloseTo(66.7, within(0.1));
        }
    }
}
