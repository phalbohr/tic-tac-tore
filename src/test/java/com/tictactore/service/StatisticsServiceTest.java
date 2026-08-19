package com.tictactore.service;

import com.tictactore.dto.PagedResponse;
import com.tictactore.dto.TeamPairStatsResponse;
import com.tictactore.dto.TimePeriod;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.repository.projection.TeamPairStatsProjection;
import com.tictactore.service.impl.StatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService Unit Tests")
class StatisticsServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsServiceImpl(matchRepository, userRepository);
    }

    @Test
    @DisplayName("Should return paginated team pair statistics with resolved player names")
    void shouldReturnPaginatedTeamPairStats_whenDataExists() {
        UUID attackerId = UUID.randomUUID();
        UUID defenderId = UUID.randomUUID();

        User attacker = User.builder().id(attackerId).nickname("Alice").avatar("avatar1.png").build();
        User defender = User.builder().id(defenderId).nickname("Bob").avatar("avatar2.png").build();

        TeamPairStatsProjection projection = mock(TeamPairStatsProjection.class);
        when(projection.getAttackerId()).thenReturn(attackerId.toString());
        when(projection.getDefenderId()).thenReturn(defenderId.toString());
        when(projection.getMatches()).thenReturn(10L);
        when(projection.getWins()).thenReturn(7L);
        when(projection.getLosses()).thenReturn(3L);
        when(projection.getWinRate()).thenReturn(70.0);

        PageRequest pageRequest = PageRequest.of(0, 10);
        when(matchRepository.aggregateTeamPairStats(eq(null), any(Instant.class), eq(null), eq(1), eq(pageRequest)))
                .thenReturn(new PageImpl<>(List.of(projection), pageRequest, 1));
        when(userRepository.findAllById(any())).thenReturn(List.of(attacker, defender));

        PagedResponse<TeamPairStatsResponse> result = statisticsService.getTeamPairStats(
                null, TimePeriod.LAST_MONTH, null, 0, 10, 1
        );

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        TeamPairStatsResponse item = result.content().get(0);
        assertThat(item.attackerId()).isEqualTo(attackerId);
        assertThat(item.attackerName()).isEqualTo("Alice");
        assertThat(item.attackerAvatar()).isEqualTo("avatar1.png");
        assertThat(item.defenderId()).isEqualTo(defenderId);
        assertThat(item.defenderName()).isEqualTo("Bob");
        assertThat(item.defenderAvatar()).isEqualTo("avatar2.png");
        assertThat(item.matches()).isEqualTo(10L);
        assertThat(item.wins()).isEqualTo(7L);
        assertThat(item.losses()).isEqualTo(3L);
        assertThat(item.winRate()).isEqualTo(70.0);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should filter by specific playerId when provided")
    void shouldFilterByPlayerId_whenSpecified() {
        UUID targetPlayerId = UUID.randomUUID();
        UUID otherPlayerId = UUID.randomUUID();

        TeamPairStatsProjection projection = mock(TeamPairStatsProjection.class);
        when(projection.getAttackerId()).thenReturn(targetPlayerId.toString());
        when(projection.getDefenderId()).thenReturn(otherPlayerId.toString());
        when(projection.getMatches()).thenReturn(5L);
        when(projection.getWins()).thenReturn(4L);
        when(projection.getLosses()).thenReturn(1L);
        when(projection.getWinRate()).thenReturn(80.0);

        PageRequest pageRequest = PageRequest.of(0, 10);
        when(matchRepository.aggregateTeamPairStats(eq(targetPlayerId), any(Instant.class), eq(null), eq(5), eq(pageRequest)))
                .thenReturn(new PageImpl<>(List.of(projection), pageRequest, 1));
        when(userRepository.findAllById(any())).thenReturn(List.of());

        PagedResponse<TeamPairStatsResponse> result = statisticsService.getTeamPairStats(
                targetPlayerId, TimePeriod.LAST_WEEK, null, 0, 10, 5
        );

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).attackerId()).isEqualTo(targetPlayerId);
    }

    @Test
    @DisplayName("Should return H2H stats correctly aggregating With, Vs, and Positional Goals")
    void shouldReturnHeadToHeadStats_withVsAndWithMatches() {
        UUID playerId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();
        UUID teammateId = UUID.randomUUID();
        UUID otherOpponentId = UUID.randomUUID();

        User opponent = User.builder().id(opponentId).nickname("Rival").avatar("rival.png").build();
        when(userRepository.findById(opponentId)).thenReturn(java.util.Optional.of(opponent));

        // Match 1: 2v2 "With" (Player & Opponent in Team A, win 2-1 games)
        com.tictactore.model.Match matchWith = com.tictactore.model.Match.builder()
                .id(UUID.randomUUID())
                .status("CONFIRMED")
                .teamAAttackerId(playerId)
                .teamADefenderId(opponentId)
                .teamBAttackerId(teammateId)
                .teamBDefenderId(otherOpponentId)
                .createdAt(Instant.now())
                .build();
        com.tictactore.model.Game g1 = com.tictactore.model.Game.builder().match(matchWith).teamAScore(10).teamBScore(5).build();
        com.tictactore.model.Game g2 = com.tictactore.model.Game.builder().match(matchWith).teamAScore(8).teamBScore(10).build();
        com.tictactore.model.Game g3 = com.tictactore.model.Game.builder().match(matchWith).teamAScore(10).teamBScore(7).build();
        matchWith.setGames(List.of(g1, g2, g3));

        // Match 2: 2v2 "Vs" (Player is Attacker in Team A, Opponent is Defender in Team B, win 2-0 games)
        com.tictactore.model.Match matchVs = com.tictactore.model.Match.builder()
                .id(UUID.randomUUID())
                .status("CONFIRMED")
                .teamAAttackerId(playerId)
                .teamADefenderId(teammateId)
                .teamBAttackerId(otherOpponentId)
                .teamBDefenderId(opponentId)
                .createdAt(Instant.now())
                .build();
        com.tictactore.model.Game g4 = com.tictactore.model.Game.builder().match(matchVs).teamAScore(10).teamBScore(4).build();
        com.tictactore.model.Game g5 = com.tictactore.model.Game.builder().match(matchVs).teamAScore(10).teamBScore(6).build();
        matchVs.setGames(List.of(g4, g5));

        when(matchRepository.findHeadToHeadMatches(eq(playerId), eq(opponentId), any(), eq(null), eq(null)))
                .thenReturn(List.of(matchWith, matchVs));

        com.tictactore.dto.H2HStatsResponse response = statisticsService.getHeadToHeadStats(
                playerId, opponentId, TimePeriod.ALL_TIME, null, null
        );

        assertThat(response).isNotNull();
        assertThat(response.opponent().id()).isEqualTo(opponentId);
        assertThat(response.opponent().nickname()).isEqualTo("Rival");
        assertThat(response.opponent().avatarUrl()).isEqualTo("rival.png");

        // Matches With: 1 match, 1 win, 0 loss, 100% win rate
        assertThat(response.matches().with().matches()).isEqualTo(1);
        assertThat(response.matches().with().wins()).isEqualTo(1);
        assertThat(response.matches().with().losses()).isEqualTo(0);
        assertThat(response.matches().with().winRate()).isEqualTo(100.0);

        // Matches Vs: 1 match, 1 win, 0 loss, 100% win rate
        assertThat(response.matches().vs().matches()).isEqualTo(1);
        assertThat(response.matches().vs().wins()).isEqualTo(1);
        assertThat(response.matches().vs().losses()).isEqualTo(0);
        assertThat(response.matches().vs().winRate()).isEqualTo(100.0);

        // Games With: 3 total games, 2 won, 1 lost, 66.7% win rate
        assertThat(response.games().with().totalGames()).isEqualTo(3);
        assertThat(response.games().with().gamesWon()).isEqualTo(2);
        assertThat(response.games().with().gamesLost()).isEqualTo(1);
        assertThat(response.games().with().winRate()).isEqualTo(66.7);

        // Games Vs: 2 total games, 2 won, 0 lost, 100% win rate
        assertThat(response.games().vs().totalGames()).isEqualTo(2);
        assertThat(response.games().vs().gamesWon()).isEqualTo(2);
        assertThat(response.games().vs().gamesLost()).isEqualTo(0);
        assertThat(response.games().vs().winRate()).isEqualTo(100.0);

        // Goals: In matchVs, Player=Attacker, Opponent=Defender => Attacker vs Defender scored: 10+10=20, conceded: 4+6=10
        assertThat(response.goals().attackerVsDefender().scored()).isEqualTo(20);
        assertThat(response.goals().attackerVsDefender().conceded()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return empty stats when 0 matches found")
    void shouldReturnEmptyStats_whenNoMatches() {
        UUID playerId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();

        when(userRepository.findById(opponentId)).thenReturn(java.util.Optional.empty());
        when(matchRepository.findHeadToHeadMatches(eq(playerId), eq(opponentId), any(), any(), any()))
                .thenReturn(List.of());

        com.tictactore.dto.H2HStatsResponse response = statisticsService.getHeadToHeadStats(
                playerId, opponentId, TimePeriod.ALL_TIME, null, "1v1"
        );

        assertThat(response).isNotNull();
        assertThat(response.opponent().nickname()).isEqualTo("A player");
        assertThat(response.matches().with().matches()).isEqualTo(0);
        assertThat(response.matches().vs().matches()).isEqualTo(0);
        assertThat(response.games().with().totalGames()).isEqualTo(0);
        assertThat(response.games().vs().totalGames()).isEqualTo(0);
        assertThat(response.goals().attackerVsDefender().scored()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should resolve ex-player nickname as retired player")
    void shouldResolveExPlayerAsRetiredPlayer() {
        UUID playerId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();

        User opponent = User.builder().id(opponentId).nickname("ex-player-12345").build();
        when(userRepository.findById(opponentId)).thenReturn(java.util.Optional.of(opponent));
        when(matchRepository.findHeadToHeadMatches(eq(playerId), eq(opponentId), any(), any(), any()))
                .thenReturn(List.of());

        com.tictactore.dto.H2HStatsResponse response = statisticsService.getHeadToHeadStats(
                playerId, opponentId, TimePeriod.ALL_TIME, null, null
        );

        assertThat(response.opponent().nickname()).isEqualTo("A retired player");
    }
}
