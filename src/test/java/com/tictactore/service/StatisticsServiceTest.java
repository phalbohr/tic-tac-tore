package com.tictactore.service;

import com.tictactore.dto.statistics.PlayerStatsResponse;
import com.tictactore.dto.statistics.PositionStatsResponse;
import com.tictactore.model.*;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StatisticsServiceTest {

    private StatisticsService statisticsService;
    private MatchRepository matchRepository;
    private UserRepository userRepository;
    private User player;
    private User other;

    @BeforeEach
    void setUp() {
        matchRepository = mock(MatchRepository.class);
        userRepository = mock(UserRepository.class);
        statisticsService = new StatisticsService(matchRepository, userRepository);
        player = new User();
        player.setId(UUID.randomUUID());
        player.setName("Player");
        player.setEmail("player@example.com");

        other = new User();
        other.setId(UUID.randomUUID());
        other.setName("Other");
    }

    @Test
    void calculateStatsFromMatches_MultipleMatchesAndPositions() {
        // Given
        // Match 1: Player as Team A Attacker, Team A wins (2-0 games)
        Match match1 = createMatch(player, other, other, other, WinnerTeam.TEAM_A);
        
        // Match 2: Player as Team B Defender, Team B wins (2-1 games)
        Match match2 = createMatch(other, other, other, player, WinnerTeam.TEAM_B);

        // Match 3: Player as Team A Attacker, Team B wins (0-2 games)
        Match match3 = createMatch(player, other, other, other, WinnerTeam.TEAM_B);

        List<Match> matches = List.of(match1, match2, match3);

        // When
        PlayerStatsResponse stats = statisticsService.calculateStatsFromMatches(player, matches);

        // Then
        assertThat(stats.playerId()).isEqualTo(player.getId());
        
        // Overall: 3 matches, 2 wins (m1, m2), 1 loss (m3). Win rate = 66.67%
        assertStats(stats.overall(), 3L, 2L, 1L, 66.67);
        
        // Attacker: 2 matches (m1, m3), 1 win (m1), 1 loss (m3). Win rate = 50.0%
        assertStats(stats.attacker(), 2L, 1L, 1L, 50.0);
        
        // Defender: 1 match (m2), 1 win (m2), 0 losses. Win rate = 100.0%
        assertStats(stats.defender(), 1L, 1L, 0L, 100.0);
    }

    private Match createMatch(User aAtt, User aDef, User bAtt, User bDef, WinnerTeam winner) {
        Match match = new Match();
        match.setId(UUID.randomUUID());
        match.setTeamAAttacker(aAtt);
        match.setTeamADefender(aDef);
        match.setTeamBAttacker(bAtt);
        match.setTeamBDefender(bDef);
        match.setWinner(winner);
        match.setStatus(MatchStatus.CONFIRMED);
        return match;
    }

    private void assertStats(PositionStatsResponse stats, Long m, Long w, Long l, Double wr) {
        assertThat(stats.matches()).isEqualTo(m);
        assertThat(stats.wins()).isEqualTo(w);
        assertThat(stats.losses()).isEqualTo(l);
        assertThat(stats.winRate()).isCloseTo(wr, org.assertj.core.data.Offset.offset(0.01));
    }
}
