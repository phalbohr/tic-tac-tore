package com.tictactore.service.achievement;

import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.service.achievement.evaluator.CleanSheetEvaluator;
import com.tictactore.service.achievement.evaluator.DefenseWallEvaluator;
import com.tictactore.service.achievement.evaluator.FirstWinEvaluator;
import com.tictactore.service.achievement.evaluator.MatchesPlayedEvaluator;
import com.tictactore.service.achievement.evaluator.StrikerGoalsEvaluator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Achievement Evaluator Unit Tests")
class AchievementEvaluatorTest {

    private final FirstWinEvaluator firstWinEvaluator = new FirstWinEvaluator();
    private final MatchesPlayedEvaluator matchesPlayedEvaluator = new MatchesPlayedEvaluator();
    private final CleanSheetEvaluator cleanSheetEvaluator = new CleanSheetEvaluator();
    private final StrikerGoalsEvaluator strikerGoalsEvaluator = new StrikerGoalsEvaluator();
    private final DefenseWallEvaluator defenseWallEvaluator = new DefenseWallEvaluator();

    @Nested
    @DisplayName("FirstWinEvaluator")
    class FirstWinTests {

        @Test
        @DisplayName("should return true when player has at least one historical win")
        void shouldReturnTrueWhenPlayerHasHistoricalWin() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 5, 1, 10, 2);

            var result = firstWinEvaluator.evaluate(userId, null, stats);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return true when player won the current match")
        void shouldReturnTrueWhenPlayerWonCurrentMatch() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game = Game.builder().teamAScore(10).teamBScore(5).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game))
                    .build();
            var stats = new PlayerStatsContext(userId, 0, 0, 0, 0);

            var result = firstWinEvaluator.evaluate(userId, match, stats);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when player lost and has zero historical wins")
        void shouldReturnFalseWhenPlayerLost() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game = Game.builder().teamAScore(3).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game))
                    .build();
            var stats = new PlayerStatsContext(userId, 1, 0, 3, 0);

            var result = firstWinEvaluator.evaluate(userId, match, stats);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("MatchesPlayedEvaluator")
    class MatchesPlayedTests {

        @Test
        @DisplayName("should return true when player participated in 10 or more matches")
        void shouldReturnTrueWhenMatchesGte10() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 10, 5, 20, 4);

            var result = matchesPlayedEvaluator.evaluate(userId, null, stats);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when player has played fewer than 10 matches")
        void shouldReturnFalseWhenMatchesLt10() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 9, 3, 15, 2);

            var result = matchesPlayedEvaluator.evaluate(userId, null, stats);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("CleanSheetEvaluator")
    class CleanSheetTests {

        @Test
        @DisplayName("should return true when winning team conceded zero goals across all games")
        void shouldReturnTrueWhenConcededZeroGoals() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(5).teamBScore(0).build();
            var game2 = Game.builder().teamAScore(5).teamBScore(0).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2))
                    .build();

            var result = cleanSheetEvaluator.evaluate(userId, match, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when opponent scored at least one goal")
        void shouldReturnFalseWhenOpponentScored() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(5).teamBScore(1).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1))
                    .build();

            var result = cleanSheetEvaluator.evaluate(userId, match, null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("StrikerGoalsEvaluator")
    class StrikerGoalsTests {

        @Test
        @DisplayName("should return true when total attacker goals are at least 50")
        void shouldReturnTrueWhenGoalsGte50() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 15, 10, 50, 2);

            var result = strikerGoalsEvaluator.evaluate(userId, null, stats);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when total attacker goals are under 50")
        void shouldReturnFalseWhenGoalsLt50() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 15, 10, 49, 2);

            var result = strikerGoalsEvaluator.evaluate(userId, null, stats);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("DefenseWallEvaluator")
    class DefenseWallTests {

        @Test
        @DisplayName("should return true when player played at least 10 matches as defender")
        void shouldReturnTrueWhenDefenderMatchesGte10() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 12, 6, 10, 10);

            var result = defenseWallEvaluator.evaluate(userId, null, stats);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when defender matches are under 10")
        void shouldReturnFalseWhenDefenderMatchesLt10() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 12, 6, 10, 9);

            var result = defenseWallEvaluator.evaluate(userId, null, stats);

            assertThat(result).isFalse();
        }
    }
}
