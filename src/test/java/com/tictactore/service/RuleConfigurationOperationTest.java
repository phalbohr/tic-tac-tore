package com.tictactore.service;

import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.model.MatchFormat;
import com.tictactore.model.PointDistribution;
import com.tictactore.model.PositionSwapRule;
import com.tictactore.model.RestartRule;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.model.SideSwapRule;
import com.tictactore.model.WinByTwoRule;
import com.tictactore.repository.RuleConfigurationRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuleConfigurationOperation Unit Tests")
class RuleConfigurationOperationTest {

    @Mock
    private RuleConfigurationRepository repository;

    @InjectMocks
    private RuleConfigurationOperation operation;

    private final UUID userId = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private final UUID ruleId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private RuleConfigurationRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = RuleConfigurationRequest.builder()
                .name("Office Fast 7")
                .matchFormat(MatchFormat.BEST_OF_N)
                .goalLimit(7)
                .gameLimit(1)
                .gamesToWin(1)
                .winByTwoRule(WinByTwoRule.ALL_GAMES)
                .absoluteScoreCap(10)
                .timeoutsPerGame(1)
                .timeoutDurationSeconds(20)
                .possessionLimit5BarSeconds(10)
                .possessionLimitOtherSeconds(15)
                .sideSwapRule(SideSwapRule.NONE)
                .restartRule(RestartRule.CONCEDING_TEAM)
                .spinningAllowed(false)
                .aerialsAllowed(false)
                .positionSwapRule(PositionSwapRule.FREE)
                .pointDistribution(PointDistribution.WIN_LOSS_2_0)
                .build();
    }

    @Test
    @DisplayName("createCustomRule should persist and return new rule configuration")
    void shouldCreateCustomRule_whenValid() {
        when(repository.countByCreatedBy(userId)).thenReturn(5L);
        when(repository.existsByCreatedByAndNameIgnoreCase(userId, "Office Fast 7")).thenReturn(false);
        when(repository.save(any(RuleConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RuleConfiguration result = operation.createCustomRule(sampleRequest, userId);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Office Fast 7");
        assertThat(result.getType()).isEqualTo(RuleConfigurationType.CUSTOM);
        assertThat(result.getCreatedBy()).isEqualTo(userId);
        assertThat(result.getMatchFormat()).isEqualTo(MatchFormat.BEST_OF_N);
        assertThat(result.getGoalLimit()).isEqualTo(7);
        assertThat(result.getGamesToWin()).isEqualTo(1);
        assertThat(result.getWinByTwoRule()).isEqualTo(WinByTwoRule.ALL_GAMES);
        assertThat(result.getAbsoluteScoreCap()).isEqualTo(10);
        verify(repository).save(any(RuleConfiguration.class));
    }

    @Test
    @DisplayName("createCustomRule should throw IllegalArgumentException when absoluteScoreCap is set but winByTwo is false")
    void shouldThrowException_whenAbsoluteScoreCapSetWithoutWinByTwo() {
        RuleConfigurationRequest invalidRequest = RuleConfigurationRequest.builder()
                .name("Invalid Cap")
                .matchFormat(MatchFormat.BEST_OF_N)
                .goalLimit(5)
                .gameLimit(3)
                .gamesToWin(2)
                .winByTwoRule(WinByTwoRule.NONE)
                .absoluteScoreCap(8)
                .timeoutsPerGame(2)
                .timeoutDurationSeconds(30)
                .possessionLimit5BarSeconds(10)
                .possessionLimitOtherSeconds(15)
                .sideSwapRule(SideSwapRule.BETWEEN_GAMES)
                .restartRule(RestartRule.CONCEDING_TEAM)
                .spinningAllowed(false)
                .aerialsAllowed(false)
                .positionSwapRule(PositionSwapRule.BETWEEN_GAMES)
                .pointDistribution(PointDistribution.WIN_LOSS_3_0)
                .build();

        assertThatThrownBy(() -> operation.createCustomRule(invalidRequest, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires win-by-two");
    }

    @Test
    @DisplayName("createCustomRule should throw IllegalArgumentException when absoluteScoreCap <= goalLimit")
    void shouldThrowException_whenAbsoluteScoreCapLessOrEqualToGoalLimit() {
        RuleConfigurationRequest invalidRequest = RuleConfigurationRequest.builder()
                .name("Invalid Cap 2")
                .matchFormat(MatchFormat.BEST_OF_N)
                .goalLimit(5)
                .gameLimit(3)
                .gamesToWin(2)
                .winByTwoRule(WinByTwoRule.ALL_GAMES)
                .absoluteScoreCap(5)
                .timeoutsPerGame(2)
                .timeoutDurationSeconds(30)
                .possessionLimit5BarSeconds(10)
                .possessionLimitOtherSeconds(15)
                .sideSwapRule(SideSwapRule.BETWEEN_GAMES)
                .restartRule(RestartRule.CONCEDING_TEAM)
                .spinningAllowed(false)
                .aerialsAllowed(false)
                .positionSwapRule(PositionSwapRule.BETWEEN_GAMES)
                .pointDistribution(PointDistribution.WIN_LOSS_3_0)
                .build();

        assertThatThrownBy(() -> operation.createCustomRule(invalidRequest, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than goal limit");
    }

    @Test
    @DisplayName("createCustomRule should throw IllegalArgumentException when quota of 20 is exceeded")
    void shouldThrowException_whenQuotaExceeded() {
        when(repository.countByCreatedBy(userId)).thenReturn(20L);

        assertThatThrownBy(() -> operation.createCustomRule(sampleRequest, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quota exceeded");
    }

    @Test
    @DisplayName("createCustomRule should throw IllegalArgumentException when duplicate name exists")
    void shouldThrowException_whenDuplicateName() {
        when(repository.countByCreatedBy(userId)).thenReturn(5L);
        when(repository.existsByCreatedByAndNameIgnoreCase(userId, "Office Fast 7")).thenReturn(true);

        assertThatThrownBy(() -> operation.createCustomRule(sampleRequest, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("deleteCustomRule should call repository deleteById")
    void shouldDeleteCustomRule() {
        operation.deleteCustomRule(ruleId);

        verify(repository).deleteById(ruleId);
    }
}
