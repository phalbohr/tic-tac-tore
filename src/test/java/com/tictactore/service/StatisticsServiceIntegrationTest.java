package com.tictactore.service;

import com.tictactore.dto.PagedResponse;
import com.tictactore.dto.TeamPairStatsResponse;
import com.tictactore.dto.TimePeriod;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("StatisticsService Integration Tests")
class StatisticsServiceIntegrationTest {

    @MockBean
    private RedissonClient redissonClient;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    private User playerA;
    private User playerB;
    private User playerC;
    private User playerD;

    @BeforeEach
    void setUp() {
        matchRepository.deleteAll();
        userRepository.deleteAll();

        playerA = userRepository.save(User.builder().nickname("Alice").email("alice@test.com").avatar("a.png").build());
        playerB = userRepository.save(User.builder().nickname("Bob").email("bob@test.com").avatar("b.png").build());
        playerC = userRepository.save(User.builder().nickname("Charlie").email("charlie@test.com").avatar("c.png").build());
        playerD = userRepository.save(User.builder().nickname("Diana").email("diana@test.com").avatar("d.png").build());
    }

    @Test
    @DisplayName("Should correctly aggregate pair wins, losses, winRate, and differentiate positional synergies")
    void shouldDifferentiatePositionalSynergiesAndCalculateStats() {
        // Match 1: Team A (A=Attacker, B=Defender) vs Team B (C=Attacker, D=Defender) -> Team A wins
        Match m1 = Match.builder()
                .creatorId(playerA.getId())
                .teamAAttackerId(playerA.getId())
                .teamADefenderId(playerB.getId())
                .teamBAttackerId(playerC.getId())
                .teamBDefenderId(playerD.getId())
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build();
        m1.addGame(Game.builder().gameOrder(1).teamAScore(10).teamBScore(5).build());
        matchRepository.save(m1);

        // Match 2: Team A (A=Attacker, B=Defender) vs Team B (C=Attacker, D=Defender) -> Team A wins
        Match m2 = Match.builder()
                .creatorId(playerA.getId())
                .teamAAttackerId(playerA.getId())
                .teamADefenderId(playerB.getId())
                .teamBAttackerId(playerC.getId())
                .teamBDefenderId(playerD.getId())
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build();
        m2.addGame(Game.builder().gameOrder(1).teamAScore(10).teamBScore(8).build());
        matchRepository.save(m2);

        // Match 3: Swapped positions! Team A (B=Attacker, A=Defender) vs Team B (C=Attacker, D=Defender) -> Team B wins
        Match m3 = Match.builder()
                .creatorId(playerB.getId())
                .teamAAttackerId(playerB.getId())
                .teamADefenderId(playerA.getId())
                .teamBAttackerId(playerC.getId())
                .teamBDefenderId(playerD.getId())
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build();
        m3.addGame(Game.builder().gameOrder(1).teamAScore(4).teamBScore(10).build());
        matchRepository.save(m3);

        // Unconfirmed match (should be ignored per AD-02)
        Match mPending = Match.builder()
                .creatorId(playerA.getId())
                .teamAAttackerId(playerA.getId())
                .teamADefenderId(playerB.getId())
                .teamBAttackerId(playerC.getId())
                .teamBDefenderId(playerD.getId())
                .status(Match.STATUS_PENDING_APPROVAL)
                .createdAt(Instant.now())
                .build();
        mPending.addGame(Game.builder().gameOrder(1).teamAScore(10).teamBScore(0).build());
        matchRepository.save(mPending);

        PagedResponse<TeamPairStatsResponse> result = statisticsService.getTeamPairStats(
                null, TimePeriod.ALL_TIME, null, 0, 10, 1
        );

        assertThat(result.content()).hasSize(3);

        // Find (A=Attacker, B=Defender)
        TeamPairStatsResponse pairAB = result.content().stream()
                .filter(p -> p.attackerId().equals(playerA.getId()) && p.defenderId().equals(playerB.getId()))
                .findFirst().orElseThrow();
        assertThat(pairAB.matches()).isEqualTo(2);
        assertThat(pairAB.wins()).isEqualTo(2);
        assertThat(pairAB.losses()).isEqualTo(0);
        assertThat(pairAB.winRate()).isEqualTo(100.0);
        assertThat(pairAB.attackerName()).isEqualTo("Alice");
        assertThat(pairAB.defenderName()).isEqualTo("Bob");

        // Find (B=Attacker, A=Defender)
        TeamPairStatsResponse pairBA = result.content().stream()
                .filter(p -> p.attackerId().equals(playerB.getId()) && p.defenderId().equals(playerA.getId()))
                .findFirst().orElseThrow();
        assertThat(pairBA.matches()).isEqualTo(1);
        assertThat(pairBA.wins()).isEqualTo(0);
        assertThat(pairBA.losses()).isEqualTo(1);
        assertThat(pairBA.winRate()).isEqualTo(0.0);
        assertThat(pairBA.attackerName()).isEqualTo("Bob");
        assertThat(pairBA.defenderName()).isEqualTo("Alice");

        // Find (C=Attacker, D=Defender)
        TeamPairStatsResponse pairCD = result.content().stream()
                .filter(p -> p.attackerId().equals(playerC.getId()) && p.defenderId().equals(playerD.getId()))
                .findFirst().orElseThrow();
        assertThat(pairCD.matches()).isEqualTo(3);
        assertThat(pairCD.wins()).isEqualTo(1);
        assertThat(pairCD.losses()).isEqualTo(2);
        assertThat(pairCD.winRate()).isEqualTo(33.33);
    }

    @Test
    @DisplayName("Should filter by minMatches threshold")
    void shouldFilterByMinMatches() {
        Match m1 = Match.builder()
                .creatorId(playerA.getId())
                .teamAAttackerId(playerA.getId())
                .teamADefenderId(playerB.getId())
                .teamBAttackerId(playerC.getId())
                .teamBDefenderId(playerD.getId())
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build();
        m1.addGame(Game.builder().gameOrder(1).teamAScore(10).teamBScore(5).build());
        matchRepository.save(m1);

        PagedResponse<TeamPairStatsResponse> result = statisticsService.getTeamPairStats(
                null, TimePeriod.ALL_TIME, null, 0, 10, 2
        );

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should aggregate real database matches for Head to Head statistics")
    void shouldAggregateRealDatabaseMatchesForHeadToHead() {
        // Match 1 (Vs): A & B vs C & D (A Attacker, B Defender; C Attacker, D Defender) -> A&B wins 10-5
        Match m1 = Match.builder()
                .creatorId(playerA.getId())
                .teamAAttackerId(playerA.getId())
                .teamADefenderId(playerB.getId())
                .teamBAttackerId(playerC.getId())
                .teamBDefenderId(playerD.getId())
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build();
        m1.addGame(Game.builder().gameOrder(1).teamAScore(10).teamBScore(5).build());
        matchRepository.save(m1);

        // Match 2 (With): A & C vs B & D (A Attacker, C Defender; B Attacker, D Defender) -> A&C wins 10-8
        Match m2 = Match.builder()
                .creatorId(playerA.getId())
                .teamAAttackerId(playerA.getId())
                .teamADefenderId(playerC.getId())
                .teamBAttackerId(playerB.getId())
                .teamBDefenderId(playerD.getId())
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build();
        m2.addGame(Game.builder().gameOrder(1).teamAScore(10).teamBScore(8).build());
        matchRepository.save(m2);

        // Fetch H2H for Player A vs Player C
        com.tictactore.dto.H2HStatsResponse h2h = statisticsService.getHeadToHeadStats(
                playerA.getId(), playerC.getId(), TimePeriod.ALL_TIME, null, null
        );

        assertThat(h2h.opponent().id()).isEqualTo(playerC.getId());
        assertThat(h2h.opponent().nickname()).isEqualTo("Charlie");

        // With (m2): 1 match, 1 win
        assertThat(h2h.matches().with().matches()).isEqualTo(1);
        assertThat(h2h.matches().with().wins()).isEqualTo(1);
        assertThat(h2h.matches().with().winRate()).isEqualTo(100.0);

        // Vs (m1): 1 match, 1 win
        assertThat(h2h.matches().vs().matches()).isEqualTo(1);
        assertThat(h2h.matches().vs().wins()).isEqualTo(1);
        assertThat(h2h.matches().vs().winRate()).isEqualTo(100.0);

        // Goals in Vs (m1): Player A is Attacker, Player C is Attacker => Attacker vs Attacker scored: 10, conceded: 5
        assertThat(h2h.goals().attackerVsAttacker().scored()).isEqualTo(10);
        assertThat(h2h.goals().attackerVsAttacker().conceded()).isEqualTo(5);
    }
}
