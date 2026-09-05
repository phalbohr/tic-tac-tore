package com.tictactore.service;

import com.tictactore.dto.GameDto;
import com.tictactore.exception.InvalidMatchScoreException;
import com.tictactore.model.MatchFormat;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.WinByTwoRule;
import com.tictactore.service.impl.MatchScoreValidatorImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MatchScoreValidator Unit Tests")
class MatchScoreValidatorTest {

    private MatchScoreValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MatchScoreValidatorImpl();
    }

    @Test
    void shouldPass_whenNoRuleConfigAndStandardValidScore() {
        var games = List.of(new GameDto(10, 8, null, null, null, null));

        assertThatCode(() -> validator.validateScores(null, games))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrow_whenNoRuleConfigAndNegativeScore() {
        var games = List.of(new GameDto(-1, 8, null, null, null, null));

        assertThatThrownBy(() -> validator.validateScores(null, games))
                .isInstanceOf(InvalidMatchScoreException.class)
                .hasMessageContaining("Game scores must be between 0 and 100");
    }

    @Test
    void shouldThrow_whenNoRuleConfigAndScoreExceeds100() {
        var games = List.of(new GameDto(101, 8, null, null, null, null));

        assertThatThrownBy(() -> validator.validateScores(null, games))
                .isInstanceOf(InvalidMatchScoreException.class)
                .hasMessageContaining("Game scores must be between 0 and 100");
    }

    @Test
    void shouldThrow_whenGamesExceedRuleConfigGameLimit() {
        var ruleConfig = RuleConfiguration.builder()
                .gameLimit(3)
                .goalLimit(10)
                .winByTwoRule(WinByTwoRule.NONE)
                .build();
        var games = List.of(
                new GameDto(10, 5, null, null, null, null),
                new GameDto(10, 6, null, null, null, null),
                new GameDto(10, 7, null, null, null, null),
                new GameDto(10, 8, null, null, null, null)
        );

        assertThatThrownBy(() -> validator.validateScores(ruleConfig, games))
                .isInstanceOf(InvalidMatchScoreException.class)
                .hasMessageContaining("Match must have between 1 and 3 games");
    }

    @Test
    void shouldThrow_whenRuleConfigConfiguredAndGameIsTied() {
        var ruleConfig = RuleConfiguration.builder()
                .gameLimit(3)
                .goalLimit(10)
                .winByTwoRule(WinByTwoRule.NONE)
                .build();
        var games = List.of(new GameDto(10, 10, null, null, null, null));

        assertThatThrownBy(() -> validator.validateScores(ruleConfig, games))
                .isInstanceOf(InvalidMatchScoreException.class)
                .hasMessageContaining("Game scores cannot be tied");
    }

    @ParameterizedTest
    @CsvSource({
            "10, 5",
            "10, 9",
            "0, 10"
    })
    void shouldPass_whenWinByTwoDisabledAndWinnerReachesGoalLimit(int scoreA, int scoreB) {
        var ruleConfig = RuleConfiguration.builder()
                .gameLimit(3)
                .goalLimit(10)
                .winByTwoRule(WinByTwoRule.NONE)
                .build();
        var games = List.of(new GameDto(scoreA, scoreB, null, null, null, null));

        assertThatCode(() -> validator.validateScores(ruleConfig, games))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrow_whenWinByTwoDisabledAndWinnerExceedsGoalLimit() {
        var ruleConfig = RuleConfiguration.builder()
                .gameLimit(3)
                .goalLimit(10)
                .winByTwoRule(WinByTwoRule.NONE)
                .build();
        var games = List.of(new GameDto(11, 9, null, null, null, null));

        assertThatThrownBy(() -> validator.validateScores(ruleConfig, games))
                .isInstanceOf(InvalidMatchScoreException.class)
                .hasMessageContaining("Winning score must equal goal limit of 10 when win-by-two is disabled");
    }

    @Test
    void shouldThrow_whenWinByTwoDisabledAndWinnerDoesNotReachGoalLimit() {
        var ruleConfig = RuleConfiguration.builder()
                .gameLimit(3)
                .goalLimit(10)
                .winByTwoRule(WinByTwoRule.NONE)
                .build();
        var games = List.of(new GameDto(9, 7, null, null, null, null));

        assertThatThrownBy(() -> validator.validateScores(ruleConfig, games))
                .isInstanceOf(InvalidMatchScoreException.class)
                .hasMessageContaining("Winning score must reach at least the goal limit of 10");
    }

    @ParameterizedTest
    @CsvSource({
            "10, 8",
            "10, 0",
            "12, 10",
            "15, 13"
    })
    void shouldPass_whenWinByTwoEnabledAndValidMargin(int scoreA, int scoreB) {
        var ruleConfig = RuleConfiguration.builder()
                .gameLimit(3)
                .goalLimit(10)
                .winByTwoRule(WinByTwoRule.ALL_GAMES)
                .build();
        var games = List.of(new GameDto(scoreA, scoreB, null, null, null, null));

        assertThatCode(() -> validator.validateScores(ruleConfig, games))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrow_whenWinByTwoEnabledAndMarginLessThanTwoAtGoalLimit() {
        var ruleConfig = RuleConfiguration.builder()
                .gameLimit(3)
                .goalLimit(10)
                .winByTwoRule(WinByTwoRule.ALL_GAMES)
                .build();
        var games = List.of(new GameDto(10, 9, null, null, null, null));

        assertThatThrownBy(() -> validator.validateScores(ruleConfig, games))
                .isInstanceOf(InvalidMatchScoreException.class)
                .hasMessageContaining("Winning margin must be at least 2 goals when win-by-two is enabled");
    }

    @Test
    void shouldThrow_whenWinByTwoEnabledAndLeadNotExactlyTwoBeyondGoalLimit() {
        var ruleConfig = RuleConfiguration.builder()
                .gameLimit(3)
                .goalLimit(10)
                .winByTwoRule(WinByTwoRule.ALL_GAMES)
                .build();
        var games = List.of(new GameDto(13, 10, null, null, null, null));

        assertThatThrownBy(() -> validator.validateScores(ruleConfig, games))
                .isInstanceOf(InvalidMatchScoreException.class)
                .hasMessageContaining("Game beyond goal limit must end with exactly a 2-point lead");
    }

    @Test
    void shouldPass_whenAbsoluteScoreCapReachedEvenWithOnePointMargin() {
        var ruleConfig = RuleConfiguration.builder()
                .gameLimit(3)
                .goalLimit(5)
                .winByTwoRule(WinByTwoRule.ALL_GAMES)
                .absoluteScoreCap(8)
                .build();
        var games = List.of(new GameDto(8, 7, null, null, null, null));

        assertThatCode(() -> validator.validateScores(ruleConfig, games))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrow_whenScoreExceedsAbsoluteScoreCap() {
        var ruleConfig = RuleConfiguration.builder()
                .gameLimit(3)
                .goalLimit(5)
                .winByTwoRule(WinByTwoRule.ALL_GAMES)
                .absoluteScoreCap(8)
                .build();
        var games = List.of(new GameDto(9, 7, null, null, null, null));

        assertThatThrownBy(() -> validator.validateScores(ruleConfig, games))
                .isInstanceOf(InvalidMatchScoreException.class)
                .hasMessageContaining("Game score cannot exceed absolute score cap of 8");
    }

    @Test
    void shouldAllowOneGoalMarginInRegularGames_andRequireWinByTwoInDecisiveGame_whenDecisiveGameOnly() {
        var ruleConfig = RuleConfiguration.builder()
                .matchFormat(MatchFormat.BEST_OF_N)
                .gameLimit(5)
                .gamesToWin(3)
                .goalLimit(5)
                .winByTwoRule(WinByTwoRule.DECISIVE_GAME_ONLY)
                .absoluteScoreCap(8)
                .build();

        var regularGamesWithOneGoalMargin = List.of(
                new GameDto(5, 4, null, null, null, null), // Game 1: 5-4 valid without win-by-2
                new GameDto(4, 5, null, null, null, null), // Game 2: 4-5 valid without win-by-2
                new GameDto(5, 4, null, null, null, null), // Game 3: 5-4 valid
                new GameDto(4, 5, null, null, null, null), // Game 4: 4-5 valid (tied 2-2)
                new GameDto(8, 7, null, null, null, null)  // Game 5: decisive game reaching cap 8
        );

        assertThatCode(() -> validator.validateScores(ruleConfig, regularGamesWithOneGoalMargin))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDecisiveGameWithoutWinByTwo_whenDecisiveGameOnly() {
        var ruleConfig = RuleConfiguration.builder()
                .matchFormat(MatchFormat.BEST_OF_N)
                .gameLimit(5)
                .gamesToWin(3)
                .goalLimit(5)
                .winByTwoRule(WinByTwoRule.DECISIVE_GAME_ONLY)
                .absoluteScoreCap(8)
                .build();

        var decisiveGameInvalid = List.of(
                new GameDto(5, 4, null, null, null, null), // 1-0
                new GameDto(4, 5, null, null, null, null), // 1-1
                new GameDto(5, 4, null, null, null, null), // 2-1
                new GameDto(4, 5, null, null, null, null), // 2-2
                new GameDto(5, 4, null, null, null, null)  // Decisive game 5: 5-4 invalid (needs win by 2 or cap)
        );

        assertThatThrownBy(() -> validator.validateScores(ruleConfig, decisiveGameInvalid))
                .isInstanceOf(InvalidMatchScoreException.class)
                .hasMessageContaining("Winning margin must be at least 2 goals");
    }
}
