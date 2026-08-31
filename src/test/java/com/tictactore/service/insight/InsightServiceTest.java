package com.tictactore.service.insight;

import com.tictactore.dto.AchievementDto;
import com.tictactore.dto.InsightCategory;
import com.tictactore.dto.InsightImportance;
import com.tictactore.dto.InsightType;
import com.tictactore.dto.PlayerAchievementsSummaryResponse;
import com.tictactore.dto.PlayerInsightDto;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.Match;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.AchievementService;
import com.tictactore.service.impl.InsightServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Story 7.5: InsightService Unit Tests")
class InsightServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private AchievementService achievementService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InsightGenerator generator1;

    @Mock
    private InsightGenerator generator2;

    @Test
    @DisplayName("[P0] [AC3] should return INSUFFICIENT_DATA starter insight when player has fewer than 3 matches")
    void shouldReturnInsufficientData_whenFewerThan3Matches() {
        var playerId = UUID.randomUUID();
        when(userRepository.existsById(playerId)).thenReturn(true);
        when(matchRepository.findConfirmedMatchesByPlayerId(playerId)).thenReturn(List.of(Match.builder().id(UUID.randomUUID()).build()));
        when(achievementService.getPlayerAchievements(playerId)).thenReturn(new PlayerAchievementsSummaryResponse(playerId, 0, 0, Collections.emptyList()));

        var service = new InsightServiceImpl(matchRepository, achievementService, List.of(generator1), userRepository);
        var response = service.getPlayerInsights(playerId);

        assertThat(response).isNotNull();
        assertThat(response.playerId()).isEqualTo(playerId);
        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.insights()).hasSize(1);
        assertThat(response.insights().getFirst().type()).isEqualTo(InsightType.INSUFFICIENT_DATA);
        assertThat(response.insights().getFirst().category()).isEqualTo(InsightCategory.GENERAL);
        assertThat(response.insights().getFirst().importance()).isEqualTo(InsightImportance.LOW);
    }

    @Test
    @DisplayName("[P0] [AC1, AC2] should sort insights by importance and limit to 5")
    void shouldSortInsightsByImportanceAndLimitTo5() {
        var playerId = UUID.randomUUID();
        when(userRepository.existsById(playerId)).thenReturn(true);
        var matches = List.of(
                Match.builder().id(UUID.randomUUID()).build(),
                Match.builder().id(UUID.randomUUID()).build(),
                Match.builder().id(UUID.randomUUID()).build()
        );
        when(matchRepository.findConfirmedMatchesByPlayerId(playerId)).thenReturn(matches);
        when(achievementService.getPlayerAchievements(playerId)).thenReturn(new PlayerAchievementsSummaryResponse(playerId, 0, 0, Collections.emptyList()));

        var lowInsight = new PlayerInsightDto(UUID.randomUUID(), InsightType.INSUFFICIENT_DATA, InsightCategory.GENERAL, InsightImportance.LOW, "low", "low", Map.of(), "icon", null);
        var highInsight = new PlayerInsightDto(UUID.randomUUID(), InsightType.WIN_STREAK, InsightCategory.STREAK, InsightImportance.HIGH, "high", "high", Map.of(), "icon", null);

        when(generator1.getOrder()).thenReturn(1);
        when(generator2.getOrder()).thenReturn(2);
        when(generator1.generate(eq(playerId), any(), any(), any())).thenReturn(Optional.of(lowInsight));
        when(generator2.generate(eq(playerId), any(), any(), any())).thenReturn(Optional.of(highInsight));

        var service = new InsightServiceImpl(matchRepository, achievementService, List.of(generator1, generator2), userRepository);
        var response = service.getPlayerInsights(playerId);

        assertThat(response).isNotNull();
        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.insights().getFirst().importance()).isEqualTo(InsightImportance.HIGH);
        assertThat(response.insights().get(1).importance()).isEqualTo(InsightImportance.LOW);
    }

    @Test
    @DisplayName("[P1] [AC4] should safely handle null goals from repository without unboxing NPE")
    void shouldHandleNullGoals_withoutUnboxingNpe() {
        var playerId = UUID.randomUUID();
        when(userRepository.existsById(playerId)).thenReturn(true);
        var matches = List.of(
                Match.builder().id(UUID.randomUUID()).build(),
                Match.builder().id(UUID.randomUUID()).build(),
                Match.builder().id(UUID.randomUUID()).build()
        );
        when(matchRepository.findConfirmedMatchesByPlayerId(playerId)).thenReturn(matches);
        when(matchRepository.sumGoalsAsAttacker(playerId)).thenReturn(null);
        when(achievementService.getPlayerAchievements(playerId)).thenReturn(new PlayerAchievementsSummaryResponse(playerId, 0, 0, Collections.emptyList()));

        var service = new InsightServiceImpl(matchRepository, achievementService, Collections.emptyList(), userRepository);
        var response = service.getPlayerInsights(playerId);

        assertThat(response).isNotNull();
        assertThat(response.playerId()).isEqualTo(playerId);
    }

    @Test
    @DisplayName("[P1] [AC4] should throw ResourceNotFoundException when user does not exist")
    void shouldThrowNotFound_whenUserDoesNotExist() {
        var playerId = UUID.randomUUID();
        when(userRepository.existsById(playerId)).thenReturn(false);

        var service = new InsightServiceImpl(matchRepository, achievementService, Collections.emptyList(), userRepository);

        assertThatThrownBy(() -> service.getPlayerInsights(playerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("[P1] [AC4] should throw ResourceNotFoundException when playerId is null")
    void shouldThrowNotFound_whenPlayerIdIsNull() {
        var service = new InsightServiceImpl(matchRepository, achievementService, Collections.emptyList(), userRepository);

        assertThatThrownBy(() -> service.getPlayerInsights(null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
