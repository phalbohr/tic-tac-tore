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
}
